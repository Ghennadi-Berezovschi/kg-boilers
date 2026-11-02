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

`render.yaml` defines one paid Docker web service (512 MB), a paid PostgreSQL 16 database (256 MB, 1 GB storage) and a 1 GB persistent photo disk in Frankfurt. The owner approved these resources and deployed them on 1 November 2026, as recorded below. Auto-deployment is off, so a Git push does not publish a new release through this Blueprint.

The approved infrastructure estimate is USD 13.55/month before taxes, bandwidth overages, domain registration and any upgrades: web $7, database $6, database storage $0.30, photo disk $0.25. Check the current account estimate before approving further resources. Sources: [pricing](https://render.com/pricing), [Blueprint settings](https://render.com/docs/blueprint-spec), [persistent disks](https://render.com/docs/disks). The small plan passed the local memory smoke check; review memory and latency under real traffic before increasing capacity.

When the owner approves the recurring cost:

1. Sign in to Render and connect the existing GitHub repository. Review the Blueprint's exact resources and billing estimate before creating them.
2. Enter `ADMIN_USERNAME`, a unique `ADMIN_PASSWORD` of at least 16 characters, `SPRING_MAIL_USERNAME`, and the Gmail **app password** as `SPRING_MAIL_PASSWORD` in the Render secret fields. Never commit these values. The database environment values are wired automatically. SMTP uses port 587; Render's free web tier blocks SMTP, so the prepared service is paid.
3. Deploy the reviewed commit. The `prod` profile rejects missing credentials, tests the SMTP connection, uses secure session cookies and checks database readiness through `/healthz`. The Docker process runs as UID 10001: confirm the mounted photo directory is writable by that UID before accepting traffic. Do not replace the disk with an ephemeral directory to work around a permission error.
4. Before sharing the site, choose/confirm the domain, connect it in Render and verify its automatic HTTPS certificate. Set `PUBLIC_SITE_URL=https://your-confirmed-domain` for the home page's Open Graph and Twitter metadata. Until a custom domain is configured, the app uses Render's `RENDER_EXTERNAL_URL` automatically; local development falls back to `http://localhost:8080`. The initial Render URL can be used for the deployment check while the final domain is being chosen.
5. Confirm the paid database's recovery window and disk snapshots in the dashboard. Render documents a 3-day PostgreSQL point-in-time recovery window on Hobby and daily persistent-disk snapshots. Keep a separate periodic database export and photo archive, and restore them into a disposable environment before relying on them. Do not restore over the live database as a test. [Database backups](https://render.com/docs/postgresql-backups).
6. On the hosted HTTPS URL, verify login, all ten service entries, one labelled test request, both inboxes, upload privacy, persistence after restart, and the site's appearance on a real phone. Record the deployment commit and test results. Keep any test data labelled in the admin panel.

A disk-backed single-instance service has deployment downtime. Do not promise uninterrupted deployments. Sessions are currently in memory: restarting or deploying can reset an unfinished quote; saved leads and disk-backed photos remain. Plan deployments outside busy hours.

For rollback, manually redeploy the last verified commit. Avoid destructive database changes; restoring an old database also discards newer leads, so first preserve a fresh export and investigate the failure. The current Flyway changes are additive; this release does not alter the schema.


## Live Render deployment — 1 November 2026

The site is live at **https://pro-boiler-services-london.onrender.com/**. The owner can sign in at **https://pro-boiler-services-london.onrender.com/admin/login** with the credentials entered during setup. No credentials are stored in this guide or the deployment report.

Render successfully deployed commit `0633a9ee72e5ac9c35c96f58c395712455b3b269`. The Hobby workspace contains the approved paid web service, PostgreSQL 16 database and persistent photo disk in Frankfurt. The dashboard's estimate was **USD 13.55/month** before taxes and usage overages. The custom domain remains to be chosen; the public Render URL has HTTPS and is usable now.

Hosted checks passed: `/healthz` over HTTPS, all ten service entry forms, a real postcode lookup, callback submission, admin login, test-lead visibility, service restart, health recovery and retained lead after restart. The owner confirmed receipt of both test emails sent from the hosted app. The entry `DEPLOY LIVE TEST 20261101` remains clearly labelled in the admin panel as a test; no callback is required. After checking the lead, the test admin session was signed out.

The photo disk is mounted at `/data/kg-boilers/quote-pictures`, and external PostgreSQL access is blocked. A first logical database export is available in Render's Recovery page. Point-in-time recovery has a three-day window but was still initializing at the final check; it must not be described as already usable. Photo disk snapshots run every 24 hours and retain seven days; the first snapshot had not yet appeared. No destructive live restore was attempted.

The browser confirmed the deployed home page's Open Graph URLs point at the Render domain, no broken `img` elements were found and no JavaScript warnings/errors were captured in the checked home-page log. The hosted geometry check was at the actual 1280px viewport: the temporary IAB phone-size override did not take effect, so it is not counted as a hosted mobile test. The earlier native Chrome phone emulation remains documented above. A physical phone check can now use the public URL over either Wi-Fi or mobile data.

Full non-secret results are in `output/release-qa/render-deployment-2026-11-01.json`. Database and disk exports stay outside source control. The local database and local uploads have not been migrated into the new production instance.

## Custom domain setup — 2 November 2026

The owner purchased **proboilerserviceslondon.co.uk** from IONOS and confirmed the contact email. The checkout quoted GBP 1.20 including VAT for the first year, then GBP 12/year at the current renewal price, with automatic renewal. No additional `.com` domain was purchased.

Render has verified both the apex domain and `www.proboilerserviceslondon.co.uk`; `www` redirects to the apex. IONOS DNS now has `A @ → 216.24.57.1` and `CNAME www → pro-boiler-services-london.onrender.com`, each with a 300-second TTL. The conflicting default-site A/AAAA records were replaced/disabled. Mail-related records remain in place. Google and Cloudflare DNS resolvers return the expected apex A record.

`PUBLIC_SITE_URL=https://proboilerserviceslondon.co.uk` was applied to the running service. Commit `8dff87c94f1373815c73116415c03ebf0db24a08` is live, fixing the quote page's hardcoded sharing metadata to use the configured URL and an existing hero image. All 40 quote controller tests passed. Hosted checks through the Render hostname passed for all ten service entry forms and confirmed the home/quote sharing metadata uses the custom domain.

At the last check, the `www` TLS certificate was issued and its HTTPS response redirected correctly. **The apex certificate was still pending in Render; the custom-domain HTTPS launch is not yet verified.** HTTP apex redirects to HTTPS, but HTTPS currently fails its handshake while issuance is pending. Recheck Render's Custom Domains status and `https://proboilerserviceslondon.co.uk/healthz`, then check the home page, service forms and image URLs before sharing the custom domain. Do not bypass browser certificate warnings. The original Render HTTPS address remains available.

The database's three-day point-in-time recovery now exposes the Restore action, the 1 November 22:22 UTC logical export is available, and the photo disk has a snapshot dated 2 November 00:18 UTC. No live restore was attempted.

Non-secret results are in `output/release-qa/custom-domain-2026-11-02.json`. `render.yaml` records the selected domain and public URL for future Blueprint synchronization; auto-deploy remains off.
