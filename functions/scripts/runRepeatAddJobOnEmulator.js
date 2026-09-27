/*
 * Runs the monthly RepeatAdd job against the running Firebase emulators, the same way
 * monthly_repeatAddJob does in production. The emulator doesn't run onSchedule functions,
 * so use this to check RepeatAdds created in the Android app.
 *
 *   npm run emulator:repeatAdd              every user
 *   npm run emulator:repeatAdd -- <userId>  one user
 *
 * It uses the compiled code in lib/ (the npm script builds it first) and the current time,
 * so it adds this month's expenses. Running it twice adds them twice, like the real job would.
 */

/* Set before firebase-admin is loaded, so nothing here can reach production. */
process.env.GCLOUD_PROJECT = process.env.GCLOUD_PROJECT ?? "householdexpenses2";
process.env.FIRESTORE_EMULATOR_HOST = process.env.FIRESTORE_EMULATOR_HOST ?? "127.0.0.1:5002";
process.env.FIREBASE_DATABASE_EMULATOR_HOST = process.env.FIREBASE_DATABASE_EMULATOR_HOST ?? "127.0.0.1:9000";
process.env.FIREBASE_AUTH_EMULATOR_HOST = process.env.FIREBASE_AUTH_EMULATOR_HOST ?? "127.0.0.1:9099";

const { initializeServices } = require("../lib/app/services");
const { runRepeatAddJob } = require("../lib/jobs/scheduledJobs");

const main = async () => {
  const projectId = process.env.GCLOUD_PROJECT;
  const services = initializeServices({
    appOptions: {
      projectId,
      /* FIREBASE_DATABASE_EMULATOR_HOST redirects this URL to the emulator. */
      databaseURL: `https://${projectId}-default-rtdb.firebaseio.com`,
    },
  });

  const userId = process.argv[2];
  if (userId) {
    const added = await services.repeatAddProcessor.addExpensesFromAllRepeatAdd(userId);
    console.log(`Added ${added} expenses for user ${userId}.`);
  } else {
    await runRepeatAddJob(services);
  }
};

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
