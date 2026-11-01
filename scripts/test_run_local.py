import os
from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import MagicMock, patch

import run_local


class LocalLaunchTests(unittest.TestCase):
    def test_env_is_literal_and_shell_values_win(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / '.env').write_text('DB_PASSWORD="literal$(do-not-run)"\nexport DB_USERNAME=file-user\n# comment\n')
            with patch.dict(os.environ, {'DB_USERNAME': 'shell-user'}, clear=True):
                env = run_local.read_environment(root)
            self.assertEqual(env['DB_PASSWORD'], 'literal$(do-not-run)')
            self.assertEqual(env['DB_USERNAME'], 'shell-user')

    def test_already_running_app_does_not_launch_another_process(self):
        with patch.object(run_local, 'existing_app', return_value=True), patch.object(run_local.subprocess, 'run') as execute:
            self.assertEqual(run_local.main([]), 0)
            execute.assert_not_called()

    def test_foreign_or_unresponsive_port_is_not_killed(self):
        with patch.object(run_local, 'is_listening', return_value=True), patch.object(run_local.urllib.request, 'urlopen', side_effect=TimeoutError):
            with self.assertRaisesRegex(run_local.StartupError, 'No process was killed'):
                run_local.existing_app(8080)

    def test_known_project_on_port_is_recognized(self):
        response = MagicMock()
        response.__enter__.return_value = response
        response.status = 200
        response.read.return_value = b'<title>Pro Boiler Services London</title>'
        with patch.object(run_local, 'is_listening', return_value=True), patch.object(run_local.urllib.request, 'urlopen', return_value=response):
            self.assertTrue(run_local.existing_app(8080))

    def test_java_17_is_rejected_before_build(self):
        result = subprocess.CompletedProcess([], 0, '', 'openjdk version "17.0.1"')
        with patch.object(run_local.subprocess, 'run', return_value=result):
            with self.assertRaisesRegex(run_local.StartupError, 'Java 21'):
                run_local.java_environment({'JAVA_HOME': '/fake/jdk17'})

    def test_missing_database_values_fail_before_docker(self):
        with patch.object(run_local.subprocess, 'run') as execute:
            with self.assertRaisesRegex(run_local.StartupError, 'Missing configuration'):
                run_local.prepare_database({}, start=True)
            execute.assert_not_called()

    def test_build_output_is_outside_synced_project_and_separate_per_checkout(self):
        one = run_local.build_directory(Path('/Desktop/one'))
        two = run_local.build_directory(Path('/Desktop/two'))
        self.assertNotEqual(one, two)
        self.assertNotIn('/Desktop/', str(one))
        self.assertEqual(one.name, 'target')


if __name__ == '__main__':
    unittest.main()
