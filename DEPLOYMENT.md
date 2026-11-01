# Deployment Notes

## Local startup

Use Java 21 and start Docker Desktop. On macOS, double-click `Start Project.command` in the project folder, or run:

```sh
python3 scripts/run_local.py
```

This command starts the existing PostgreSQL service, waits for readiness, makes a clean build and runs the application in the foreground. Stop it with Ctrl+C. If the project is already responding on port 8080, the command reports its URL and does not create a second instance. An occupied or unresponsive port produces a clear error; no other process is killed.

The command stores generated build output under `~/Library/Caches/kg-boilers/` on macOS (`~/.cache/kg-boilers/` on Linux), separately for each checkout. This keeps generated classes outside a potentially synced Desktop folder. The `.env` file and database remain in their existing locations. Docker volumes are never removed by the launcher.

For IntelliJ, select the shared **KgBoilers Local** configuration. Its before-launch task runs `clean compile`, preventing old duplicate class files in `target` from slowing Spring's classpath scan. PostgreSQL must already be ready; the command above prepares it when needed. Use IntelliJ's Stop/Rerun controls for the IDE process rather than starting a second terminal instance.

To check local prerequisites without building, run `python3 scripts/run_local.py --check`. Normal Maven builds still produce the deployable JAR under `target/`.

## Startup and response checks — 1 November 2026

The earlier startup check passed 353 Java tests (including all 31 opt-in release integration tests) against a separate QA database, plus 7 launcher tests. Three packaged local starts became HTTP-ready in 3.12–4.08 seconds after building, using the launcher's local JVM options. All 14 checked pages and 89 static assets returned HTTP 200. A small local concurrency check (52 read-only requests, 8 workers) had no failures. Measurements are in `output/release-qa/startup-performance-2026-11-01.json`; they are local checks, not production load guarantees.

Callback email now runs asynchronously after the lead transaction commits. A regression test blocks mail delivery and verifies that the callback response still returns and the lead is committed. SMTP has bounded connection/read/write waits; postcode lookup has two attempts with shorter timeouts. Pages are gzip-compressed when supported by the client. The unused JPA/Hibernate startup layer has been removed; quote persistence continues to use JDBC and Flyway.

## Release verification — 30 October 2026

`mvn -q clean verify` passed all 351 tests, including 30 opt-in release tests against an isolated PostgreSQL database. The release suite covers all 10 service cards and 24 complete journey variants, form errors, expired sessions, quote prices, selected extras, lead persistence, admin rendering, attachments and callback requests.

The packaged JAR was also started locally and a boiler service / CP12 request submitted through the browser. Its database row and selected extra were verified; both emails were captured by a local SMTP server. No real email was sent.

The verified artifact is `target/kg-boilers-0.0.1-SNAPSHOT.jar`. Detailed results and its SHA-256 are in `output/release-qa/release-check-2026-10-30.json`.

The release integration suite requires `KG_RELEASE_QA_DB_URL`, `KG_RELEASE_QA_DB_USERNAME` and `KG_RELEASE_QA_DB_PASSWORD`. Use a disposable database whose name begins with `kg_boilers_release_qa_`; the suite inserts test leads. Set `DB_URL` and `DB_URL_LOCAL` to that same QA URL when running the full test suite so other Spring test contexts also stay isolated. Without the opt-in environment variables, the release suite is skipped.

## Hosting configuration

Before publishing, configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `ADMIN_USERNAME`, `ADMIN_PASSWORD`, the `SPRING_MAIL_*` settings, and the persistent upload directory below. Keep credentials outside source control and outside the JAR. Flyway applies the database migrations at startup.

Local checks do not verify the hosting provider's HTTPS, DNS or actual outbound email delivery. After deployment, check those and submit one clearly identified test request on the live site, confirming that it appears in the admin panel and that both emails arrive.

## Persistent uploaded pictures

Uploaded quote pictures are stored outside the database. In production, set:

```env
QUOTE_PICTURES_DIR=/data/kg-boilers/quote-pictures
```

That directory must be on a persistent disk or hosting volume. Do not use a temporary app build folder, because many hosting platforms delete it during redeploy.

Examples:

- VPS: create `/data/kg-boilers/quote-pictures` and keep it on the server disk.
- Docker/hosting volume: mount the volume to `/data/kg-boilers/quote-pictures`.
- Local development: leave `QUOTE_PICTURES_DIR=uploads/quote-pictures`.

The app serves these files to the uploading quote session and authenticated administrators only. Other sessions receive HTTP 404. Responses use `Cache-Control: no-store`. Keep the persistent directory outside static resources; never expose it through a public file server. The protected URL is:

```text
/uploads/quote-pictures/{filename}
```


## Production release check — 1 November 2026

The final clean build passed **359 Java tests, zero skipped**, including 31 opt-in release tests, plus 7 launcher tests. All 10 service entries and 24 full journey variants are covered by the release suite. A real callback test on the separate QA database delivered its customer confirmation to the owner's personal inbox; the owner also confirmed receipt of the business notification. No customer booking was created in the working database.

The production Docker image was started with a 512 MiB memory limit against the QA database. It passed `/healthz`, all 14 checked page entries, a 56-request / 8-worker read-only check, HTTPS-forwarded secure session cookies, gzip, restart and persistent-volume checks. A custom-format PostgreSQL backup was restored to a separate QA database and row counts matched in both public tables. A photo archive was restored and the fixture's SHA-256 matched. This tests the recovery procedure locally; it does not configure a cloud backup account.

Chrome's iPhone SE emulation (375 × 667) was checked for the home page, boiler-installation postcode form, About Us/gallery and callback form/footer. No horizontal clipping was seen in these views. This is not a physical iOS/Safari test. The browser requested an absent optional `/favicon.ico` (404); the inspected form pages had no JavaScript console errors. Performance measurements are local smoke checks, not a production capacity guarantee.

Results and artifact hashes are in `output/release-qa/predeploy-check-2026-11-01.json`.

## Suggested Render setup

`render.yaml` prepares one paid Docker web service (512 MB), a paid PostgreSQL 16 database (256 MB, 1 GB storage) and a 1 GB persistent photo disk in Frankfurt. **The file has not been applied and no paid resources have been created.** Auto-deployment is off, so a Git push does not publish a new release through this Blueprint.

The initial infrastructure estimate is USD 13.55/month before taxes, bandwidth overages, domain registration and any upgrades: web $7, database $6, database storage $0.30, photo disk $0.25. Check the current account estimate before approving creation. Sources: [pricing](https://render.com/pricing), [Blueprint settings](https://render.com/docs/blueprint-spec), [persistent disks](https://render.com/docs/disks). The small plan passed the local memory smoke check; review memory and latency under real traffic before increasing capacity.

When the owner approves the recurring cost:

1. Sign in to Render and connect the existing GitHub repository. Review the Blueprint's exact resources and billing estimate before creating them.
2. Enter `ADMIN_USERNAME`, a unique `ADMIN_PASSWORD` of at least 16 characters, `SPRING_MAIL_USERNAME`, and the Gmail **app password** as `SPRING_MAIL_PASSWORD` in the Render secret fields. Never commit these values. The database environment values are wired automatically. SMTP uses port 587; Render's free web tier blocks SMTP, so the prepared service is paid.
3. Deploy the reviewed commit. The `prod` profile rejects missing credentials, tests the SMTP connection, uses secure session cookies and checks database readiness through `/healthz`. The Docker process runs as UID 10001: confirm the mounted photo directory is writable by that UID before accepting traffic. Do not replace the disk with an ephemeral directory to work around a permission error.
4. Before sharing the site, choose/confirm the domain, connect it in Render and verify its automatic HTTPS certificate. Set `PUBLIC_SITE_URL=https://your-confirmed-domain` for the home page's Open Graph and Twitter metadata. Until a custom domain is configured, the app uses Render's `RENDER_EXTERNAL_URL` automatically; local development falls back to `http://localhost:8080`. The initial Render URL can be used for the deployment check while the final domain is being chosen.
5. Confirm the paid database's recovery window and disk snapshots in the dashboard. Render documents a 3-day PostgreSQL point-in-time recovery window on Hobby and daily persistent-disk snapshots. Keep a separate periodic database export and photo archive, and restore them into a disposable environment before relying on them. Do not restore over the live database as a test. [Database backups](https://render.com/docs/postgresql-backups).
6. On the hosted HTTPS URL, verify login, all ten service entries, one labelled test request, both inboxes, upload privacy, persistence after restart, and the site's appearance on a real phone. Record the deployment commit and test results. Keep any test data labelled in the admin panel.

A disk-backed single-instance service has deployment downtime. Do not promise uninterrupted deployments. Sessions are currently in memory: restarting or deploying can reset an unfinished quote; saved leads and disk-backed photos remain. Plan deployments outside busy hours.

For rollback, manually redeploy the last verified commit. Avoid destructive database changes; restoring an old database also discards newer leads, so first preserve a fresh export and investigate the failure. The current Flyway changes are additive; this release does not alter the schema.
