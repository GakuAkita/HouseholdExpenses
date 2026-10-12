# Functions tests

| Command | What it runs | Needs |
|---|---|---|
| `npm test` | `test/unit`: pure logic with fakes | nothing |
| `npm run test:integration` | `test/integration`: jobs and services against the Firebase emulators, with a fake Gmail and fake secrets | `firebase emulators:start` (Firestore 5002, Realtime Database 9000, Auth 9099) |
| `npm run test:live` | `test/live`: the real Gmail API with your refresh token (read-only) | `GOOGLE_OAUTH_SECRETS` and `ENCRYPTED_REFRESH_TOKEN` in `../.env.local`; skipped without them |

All tests run with `TZ=UTC`, like Cloud Functions.

Integration tests create a random user (`integration-<uuid>`) per test file and delete it afterwards,
so they don't touch other data in the emulator. Secret Manager is never called: tests use a fake secret provider.

Mail fixtures in `fixtures/mail` are synthetic. See `fixtures/mail/README.md`.

## Trying the mail jobs on your real Gmail

`npm run gmail:dryrun -- <job>` runs one mail job's Gmail query on your real Gmail and shows what
each mail would turn into, without saving anything. It uses the same credentials as `test:live`.

```
npm run gmail:dryrun -- list                                  the jobs
npm run gmail:dryrun -- amazon_subscribe_monitor --days 60    one job, last 60 days (default 30)
npm run gmail:dryrun -- all --text                            every job, with the text each parser receives
npm run gmail:dryrun -- query "from:no-reply@amazon.co.jp"    any Gmail query
```

The real jobs search from their last run instead of a fixed number of days. The `amazon_subscribe`
job saves only products in your subscribe list. The dry run doesn't load that list, so it shows
every dispatched product.
