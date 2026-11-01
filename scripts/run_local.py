#!/usr/bin/env python3
"""Start the local app in the foreground with clean, non-synced build output."""
import argparse
import fcntl
import hashlib
import os
from pathlib import Path
import re
import shutil
import socket
import subprocess
import sys
import time
import urllib.request

ROOT = Path(__file__).resolve().parents[1]


class StartupError(Exception):
    pass


def read_environment(root=ROOT):
    values = {}
    dotenv = root / '.env'
    if dotenv.exists():
        for raw in dotenv.read_text().splitlines():
            line = raw.strip()
            if not line or line.startswith('#'):
                continue
            key, separator, value = line.removeprefix('export ').partition('=')
            if separator and re.fullmatch(r'[A-Za-z_][A-Za-z0-9_]*', key.strip()):
                value = value.strip()
                if len(value) > 1 and value[0] == value[-1] and value[0] in ('"', "'"):
                    value = value[1:-1]
                values[key.strip()] = value
    # Explicit shell/IDE environment takes precedence over the .env file.
    values.update(os.environ)
    return values


def build_directory(root=ROOT):
    cache = Path.home() / ('Library/Caches' if sys.platform == 'darwin' else '.cache')
    project_id = hashlib.sha256(str(root).encode()).hexdigest()[:12]
    return cache / 'kg-boilers' / project_id / 'target'


def is_listening(port):
    try:
        with socket.create_connection(('127.0.0.1', port), timeout=0.4):
            return True
    except OSError:
        return False


def existing_app(port):
    if not is_listening(port):
        return False
    try:
        with urllib.request.urlopen(f'http://127.0.0.1:{port}/', timeout=2) as response:
            if response.status == 200 and b'Pro Boiler Services London' in response.read(65536):
                return True
    except (OSError, ValueError):
        pass
    raise StartupError(f'Port {port} is occupied but the project is not responding. Stop the other run in IntelliJ or the terminal first. No process was killed.')


def java_environment(env):
    home = env.get('JAVA_HOME')
    if not home and sys.platform == 'darwin':
        result = subprocess.run(['/usr/libexec/java_home', '-v', '21'], text=True, capture_output=True)
        if result.returncode == 0:
            home = result.stdout.strip()
    java = str(Path(home) / 'bin/java') if home else shutil.which('java')
    if not java:
        raise StartupError('Java 21 is required. Set JAVA_HOME to your JDK 21 installation.')
    result = subprocess.run([java, '-version'], text=True, capture_output=True)
    version = re.search(r'version "(\d+)', result.stderr + result.stdout)
    if result.returncode or not version or int(version[1]) != 21:
        raise StartupError('Java 21 is required. Check JAVA_HOME and the project SDK in IntelliJ.')
    if home:
        env['JAVA_HOME'] = home
        env['PATH'] = str(Path(home) / 'bin') + os.pathsep + env.get('PATH', '')
    return java


def prepare_database(env, start):
    required = ('POSTGRES_DB', 'POSTGRES_USER', 'POSTGRES_PASSWORD', 'DB_USERNAME', 'DB_PASSWORD')
    missing = [key for key in required if not env.get(key)]
    if missing:
        raise StartupError('Missing configuration: ' + ', '.join(missing) + '. Fill in .env using .env.example; values will not be printed.')
    if not shutil.which('docker'):
        raise StartupError('Docker Desktop is required for the local PostgreSQL database.')
    ready = subprocess.run(['docker', 'info'], env=env, capture_output=True, timeout=10)
    if ready.returncode:
        raise StartupError('Docker is not ready. Start Docker Desktop, wait until it is running, then run this command again.')
    if start:
        print('Starting PostgreSQL and waiting for readiness...', flush=True)
        result = subprocess.run(['docker', 'compose', 'up', '-d', 'kg_boilers_postgres'], cwd=ROOT, env=env,
                                capture_output=True, timeout=90)
        if result.returncode:
            raise StartupError('Could not start PostgreSQL. Check Docker Desktop and whether the database port is occupied. Existing database volumes were not removed.')
    deadline = time.monotonic() + (60 if start else 3)
    while time.monotonic() < deadline:
        result = subprocess.run(['docker', 'compose', 'exec', '-T', 'kg_boilers_postgres', 'sh', '-c',
                                 'pg_isready -q -U "$POSTGRES_USER" -d "$POSTGRES_DB"'],
                                cwd=ROOT, env=env, capture_output=True, timeout=5)
        if result.returncode == 0:
            return
        time.sleep(0.5)
    raise StartupError('PostgreSQL did not become ready. Check its logs in Docker Desktop; do not delete the database volume.')


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true', help='Check configuration, Java, database and the app port without building')
    parser.add_argument('--build-only', action='store_true', help='Build in the local cache without starting the app')
    args = parser.parse_args(argv)
    env = read_environment()
    try:
        port = int(env.get('SERVER_PORT', '8080'))
    except ValueError as exc:
        raise StartupError('SERVER_PORT must be a number.') from exc
    if not 1 <= port <= 65535:
        raise StartupError('SERVER_PORT must be between 1 and 65535.')
    # Never start a second copy, or interrupt an existing IDE process.
    if not args.build_only and existing_app(port):
        print(f'The project is already running: http://localhost:{port}/')
        return 0
    java = java_environment(env)
    prepare_database(env, start=not args.check)
    if args.check:
        print(f'Ready to start. Java 21 and PostgreSQL are available; port {port} is free.')
        return 0
    output = build_directory()
    output.parent.mkdir(parents=True, exist_ok=True)
    lock = open(output.parent / 'startup.lock', 'a')
    try:
        fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
    except BlockingIOError as exc:
        raise StartupError('Another local launch is in progress. Wait for it to finish.') from exc
    # Keep the lock when Python is replaced by Java below.
    os.set_inheritable(lock.fileno(), True)
    maven = shutil.which('mvn') or str(ROOT / 'mvnw')
    print(f'Building a clean local copy in {output} ...', flush=True)
    result = subprocess.run([maven, '-q', f'-Dkg.build.directory={output}', '-DskipTests', 'clean', 'package'],
                            cwd=ROOT, env=env)
    if result.returncode:
        raise StartupError('The build failed. See the compiler message above; the app was not started.')
    artifact = output / 'kg-boilers-0.0.1-SNAPSHOT.jar'
    if args.build_only:
        print(f'Build ready: {artifact}')
        return 0
    if existing_app(port):
        print(f'The project is already running: http://localhost:{port}/')
        return 0
    print(f'Starting http://localhost:{port}/ (Ctrl+C stops this run).', flush=True)
    os.chdir(ROOT)
    os.execvpe(java, [java, '-XX:TieredStopAtLevel=1', '-jar', str(artifact), '--spring.profiles.active=local',
                      '--server.address=127.0.0.1', f'--server.port={port}'], env)


if __name__ == '__main__':
    try:
        sys.exit(main())
    except (StartupError, subprocess.TimeoutExpired) as exc:
        print(f'Cannot start the project: {exc}', file=sys.stderr)
        sys.exit(1)
    except KeyboardInterrupt:
        sys.exit(130)
