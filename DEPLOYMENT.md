# Deployment Notes

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
