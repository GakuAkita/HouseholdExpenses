import * as admin from "firebase-admin";

/**
 *  環境変数についてはよくわかっていないけど、ここで毎回コメントアウトでローカルとそうでないのを切り替えればよいのでは？
 */
export const initMyFirebaseAdmin = (options?: admin.AppOptions) => {
  if (admin.apps.length > 0) {
    return;
  }

  if (options) {
    console.log("Initializing Firebase Admin SDK in LOCAL (emulator) mode");
    admin.initializeApp(options);
  } else {
    console.log("Initializing Firebase Admin SDK in PRODUCTION mode");
    admin.initializeApp();
  }
};

/**
 * オプションが渡されていなかったらdeploy状態、
 * 渡されていたらローカル
 * ------------使ってない---------
 */
export const getMyFirestore = (options?: admin.AppOptions) => {
  initMyFirebaseAdmin(options);
  return admin.firestore();
};

export { admin };
