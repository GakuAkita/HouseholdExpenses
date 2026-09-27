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
