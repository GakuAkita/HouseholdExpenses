import { deleteApp, initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { getDatabase } from "firebase-admin/database";
import { getFirestore } from "firebase-admin/firestore";
import type { TestProject } from "vitest/node";

/*
 * Each test deletes its user in afterEach, but creating an Auth user also fires onUserCreate
 * in the Functions emulator. It writes users/{uid} asynchronously, sometimes after the test
 * has cleaned up, so integration users pile up in the emulator.
 * This sweeps them before and after the whole run. A write that lands after the last sweep
 * is removed by the next run.
 */

const testUserPrefix = "integration-";

const deleteAllTestUsers = async (env: Record<string, string | undefined>) => {
  /* globalSetup runs in the main process, which doesn't get test.env. The Admin SDK reads these. */
  Object.assign(process.env, env);
  const projectId = env.GCLOUD_PROJECT as string;
  const app = initializeApp(
    { projectId, databaseURL: `https://${projectId}-default-rtdb.firebaseio.com` },
    "integration-cleanup"
  );
  try {
    const usersRef = getDatabase(app).ref("users");
    const snapshot = await usersRef
      .orderByKey()
      .startAt(testUserPrefix)
      .endAt(`${testUserPrefix}`)
      .get();
    const removals: Record<string, null> = {};
    snapshot.forEach((child) => {
      removals[child.key as string] = null;
    });
    if (Object.keys(removals).length > 0) {
      await usersRef.update(removals);
    }

    const firestore = getFirestore(app);
    /* listDocuments() also returns users that only have subcollections. */
    const userDocs = await firestore.collection("users").listDocuments();
    for (const doc of userDocs.filter((d) => d.id.startsWith(testUserPrefix))) {
      await firestore.recursiveDelete(doc);
    }

    const auth = getAuth(app);
    let pageToken: string | undefined;
    do {
      const page = await auth.listUsers(1000, pageToken);
      const uids = page.users.map((u) => u.uid).filter((uid) => uid.startsWith(testUserPrefix));
      if (uids.length > 0) {
        await auth.deleteUsers(uids);
      }
      pageToken = page.pageToken;
    } while (pageToken);
  } finally {
    await deleteApp(app);
  }
};

export default async function setup(project: TestProject) {
  const env = project.config.env as Record<string, string | undefined>;
  await deleteAllTestUsers(env);
  return () => deleteAllTestUsers(env);
}
