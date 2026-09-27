# Cloud Functions

## Layout

```
src/
  index.ts        Trigger definitions only. The exported names must not change
                  (a renamed export is deployed as a new function and the old one is deleted).
  app/            services.ts: creates every service. Called on the first trigger, not at import.
  jobs/           What the triggers do: RepeatAdd, mailbox extraction, Amazon subscribe monitor,
                  new-user settings, Gmail OAuth callback. forEachUser runs a job for every user.
  mail/           Mail extraction.
    mailSource.ts   The MailSource interface: find a mail type's mails and turn one into expenses.
    sources/        One MailSource per mail type, and the registry (index.ts).
    parsers/        Parse a mail's text.
  domain/         Pure logic: RepeatAdd days, category assignment, Amazon subscribe matching.
  infra/          Talking to the outside world.
    firestore/      Firestore services.
    rtdb/           Realtime Database services.
    gmail/          Gmail API client, queries and message helpers; Google OAuth requests.
    secrets/        Google OAuth secrets from Secret Manager (loaded at most once per instance).
  shared/         Clock, RuntimeConfig (environment variables), Runtime, encryption, time helpers.
  type/, constants/  Data types and constants shared with the app.
```

Code that needs the current time, environment variables, secrets or a Gmail client gets them
from `Runtime` (`shared/runtime.ts`), so tests can replace them.

## Errors

- `infra/` methods throw when a database or API call fails, and return `null` (or an empty
  list/record) when the data doesn't exist.
- Parsers throw `MailParseError` for a mail they can't read.
- Jobs decide what one failure stops: a user (`forEachUser`), a mail type
  (`processAllMailTypeList`) or a single mail. The failure is logged and the rest continues.
  `last_exec` is saved only after a successful run, so a failed run is retried next time.

## Adding a mail type

1. Add the setting type to `AllMailType` in `type/Mailbox.ts` (and to a schedule).
2. Add a parser in `mail/parsers/`.
3. Add a `MailSource` in `mail/sources/` and register it in `mail/sources/index.ts`.
   Until it is registered, the code doesn't compile.
4. Add a fixture mail in `test/fixtures/mail/` and tests.

## Tests

See `test/README.md`. In short: `npm test` (unit), `npm run test:integration` (needs
`firebase emulators:start`), `npm run test:live` (real Gmail, opt-in).
