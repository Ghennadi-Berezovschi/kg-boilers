# Deployment Notes

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

The app serves these files at:

```text
/uploads/quote-pictures/{filename}
```
