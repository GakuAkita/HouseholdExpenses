# Household Expenses for Engineers

An Android expense tracker that records your spending for you. It reads purchase and billing emails from Gmail, picks up payment notifications on your phone, and turns them into expense entries. You don't have to type them in yourself.

## Why I built it

With an off-the-shelf expense app, you can't add the features you need or remove the ones you don't. I buy a lot of Kindle books, and every purchase sends a confirmation email. I kept wishing those emails would go into my expense log automatically, so I built an app that does it.

Any service that sends an email can be supported, so the app also handles electricity bills, Rakuten Pay receipts and more. When a service changes its email format, its parser needs updating too, so parsers are maintained as formats change.

## Features

### Automatic expense recording

Cloud Functions check each user's Gmail on a schedule and extract expenses from these emails:

| Source | What is recorded |
|---|---|
| Amazon orders | Items and amounts from order confirmations |
| Amazon Subscribe & Save | Deliveries from shipping notifications |
| Kindle | eBook purchases |
| Rakuten Pay | Store name and amount from payment confirmations |
| Rakuten Card ETC | Highway toll charges |
| Udemy | Course purchases |
| Shikoku Electric Power | Monthly electricity bills |

Categories are assigned automatically by rules you configure.

### On-device capture

- **PayPay receipt reader**: share a PayPay receipt screenshot with the app and on-device OCR (ML Kit) reads it.
- **Dr.Wallet import** *(beta, still being tested)*: import expense history from a Dr.Wallet CSV export.
- **Notification listener** *(in development, not working yet)*: will capture payment notifications from PayPay and Osaifu-Keitai (FeliCa).

### Managing expenses

- Calendar-based home screen, plus search and statistics views
- Recurring expenses (Repeat Add), added automatically every month
- Custom categories and category-assignment rules
- Time zone setting
- Real-time sync across devices via Firebase

## Architecture

```
┌───────────────────────┐        ┌──────────────────────────────┐
│  Android app          │        │  Cloud Functions (Node 20)   │
│  Kotlin + Compose     │◀──────▶│  Scheduled jobs:             │
│  Room (offline cache) │Firebase│   - Mailbox extraction ◀─────┼── Gmail API
│  Share receiver       │        │   - Monthly Repeat Add       │
│  ML Kit OCR           │        │   - Amazon Subscribe monitor │
└───────────────────────┘        │  Gmail OAuth callback        │
                                 └──────────────────────────────┘
          Firestore (expenses) · Realtime Database (settings) · Firebase Auth
```

## Tech stack

**Android app** (`app/`)
- Kotlin, Jetpack Compose, Material 3
- Room (local storage and offline cache)
- Firebase Authentication, Firestore, Realtime Database
- ML Kit Text Recognition (Japanese) for receipt OCR
- Manual dependency injection through app/session containers (`di/`)
- minSdk 31

**Backend** (`functions/`)
- TypeScript on Node.js 20, Firebase Cloud Functions
- Gmail API with per-user Google OAuth; secrets stored in Secret Manager
- Vitest for unit, integration (Firebase emulators) and live Gmail tests

See [`functions/README.md`](functions/README.md) for the backend layout and how to add support for a new email type.

## Getting started

### Prerequisites

- Android Studio (recent stable) and JDK 17+
- Node.js 20 and the [Firebase CLI](https://firebase.google.com/docs/cli)
- A Firebase project with Authentication, Firestore and Realtime Database enabled

### Configuration

These files contain project-specific settings and are not committed:

| File | Purpose |
|---|---|
| `app/google-services.json` | Firebase config for the Android app (download from the Firebase console) |
| `firebase.json` | Firebase CLI config (emulators, functions, rules) |
| `.env.local` | Optional. Credentials for the live Gmail tests (`GOOGLE_OAUTH_SECRETS`, `ENCRYPTED_REFRESH_TOKEN`) |

### Build the app

```bash
./gradlew assembleDebug
```

Or open the project in Android Studio and run the `app` configuration.

### Run the backend locally

```bash
cd functions
npm install
npm run build

# Start the emulators with the saved data in emu_data/
firebase emulators:start --import=./emu_data --export-on-exit
```

On Windows, `start_emulator.bat` does the same.

### Tests

```bash
cd functions
npm test                    # unit tests
npm run test:integration    # needs the Firebase emulators running
npm run test:live           # real Gmail API, read-only; skipped without credentials
```

See [`functions/test/README.md`](functions/test/README.md) for details.

## CI/CD

Both GitHub Actions workflows are run manually (`workflow_dispatch`):

- **Build apk** (`build_apk.yml`): tags the next version, builds and signs a release APK, and uploads it as a workflow artifact.
- **Deploy functions** (`deploy_functions.yml`): deploys Cloud Functions to Firebase.

## License

This project is licensed under the MIT License. See [LICENSE.txt](LICENSE.txt) for details.
