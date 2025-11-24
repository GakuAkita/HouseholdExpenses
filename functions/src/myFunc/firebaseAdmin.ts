import * as admin from "firebase-admin";
import { logger } from "firebase-functions";

export const initMyFirebaseAdmin = (options?: admin.AppOptions) => {
  if (admin.apps.length > 0) {
    return;
  }

  if (options) {
    /**
     * エミュレーターに接続するときは、
     * project idとdatabaseURLをいれて渡す
     */
    if (options.databaseURL === undefined) {
      logger.warn(`-----------!!!!!!databaseURL is not defined!!!!!!-----------`);
    }
    admin.initializeApp(options);

    /**
     *  FIRESTORE_EMULATOR_HOSTが環境変数で定義されていれば
     *  自動でエミュレーターに接続する。
     * */
    if (process.env.FIRESTORE_EMULATOR_HOST !== undefined) {
      logger.log(`Firestore is connected to emulator =${process.env.FIRESTORE_EMULATOR_HOST}`);
    }
  } else {
    logger.log("Initializing Firebase Admin SDK in PRODUCTION mode");
    admin.initializeApp();
  }
};

export const getMyFirestore = (options?: admin.AppOptions) => {
  initMyFirebaseAdmin(options);
  return admin.firestore();
};

export { admin };
