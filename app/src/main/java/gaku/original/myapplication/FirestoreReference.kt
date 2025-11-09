package gaku.original.myapplication

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.firestore
import javax.inject.Inject

/* @TODO throwを出してしまうとアプリがクラッシュするらしい。したがって、nullのときの回避策を作る必要ある。*/
class FirestoreReference @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) {
    val className: String = this::class.simpleName ?: "UnableToGetClassName"

    private val firestore = if (BuildConfig.DEBUG && BuildConfig.USE_FIREBASE_EMULATOR) {
        // DEBUGモードかつUSE_FIREBASE_EMULATOR=trueのときはエミュレータを使用
        // 注意: useEmulatorはFirestoreインスタンスを取得する前に呼ぶ必要がある
        // ここでFirestoreインスタンスを取得する前にuseEmulatorを呼ぶ
        try {
            val emulatorHost = BuildConfig.FIREBASE_EMULATOR_HOST // local.propertiesから読み込まれる
            Log.d(className, "🔧 Firestoreエミュレータ設定開始: host=$emulatorHost, port=5002")
            Log.d(className, "🔧 Firebase.firestoreにアクセス前")
            
            // Firebase.firestoreにアクセスする前にuseEmulatorを呼ぶ
            Firebase.firestore.useEmulator(emulatorHost, 5002)
            
            Log.d(className, "✅ Firestore emulator configured: $emulatorHost:5002")
            Log.d(className, "🔧 Firebase.firestoreにアクセス後")
            
            // エミュレータ設定後にFirestoreインスタンスを取得
            Firebase.firestore
        } catch (e: IllegalStateException) {
            // 既にエミュレータが設定されている場合は、そのままFirestoreインスタンスを取得
            Log.d(className, "⚠️ Firestore emulator already configured, using existing instance")
            Firebase.firestore
        } catch (e: Exception) {
            Log.e(className, "❌ Failed to configure Firestore emulator: ${e.message}", e)
            // エミュレータ設定に失敗した場合は通常のFirestoreを使用
            Firebase.firestore
        }
    } else {
        // 本番環境のFirestoreを使用
        if (BuildConfig.DEBUG) {
            Log.d(className, "ℹ️ DEBUGモードですが、USE_FIREBASE_EMULATOR=falseのため本番環境を使用")
        }
        Firebase.firestore
    }

    private val currentUserId: String?
        get() = if (BuildConfig.DEBUG && BuildConfig.USE_FIREBASE_EMULATOR) {
            // エミュレータ使用時はtestUserを使用（functions/src/local_emulator.tsと同じ）
            "testUser"
        } else {
            firebaseAuth.currentUser?.uid
        }

    /**
     * コレクションやドキュメント名はスネークケースの方が良い
     * その方がURLフレンドリーだから
     */

    fun getUsersColRef(): CollectionReference {
        return firestore.collection("users")
    }

    /**
     * users/{userId}
     * あ～今になっておもったけど、これだとsignUpの処理が事故るかもな。
     * 例えば、最初のサインアップして、一回止めて別のアカウントでサインアップした時、
     * デフォルトのカテゴリーを追加する処理の途中でこのuidが切り替わってしまう。
     * 途中で確実に失敗する。
     * */
    fun getUserDocRef(): DocumentReference? {
        return currentUserId?.let { getUsersColRef().document(it) }
    }

    fun getExpensesColRef(): CollectionReference? {
        return getUserDocRef()?.collection("expenses")
    }

    fun getCategoriesColRef(): CollectionReference? {
        return getUserDocRef()?.collection("categories")
    }

    fun getRepeatAddColRef(): CollectionReference? {
        return getUserDocRef()?.collection("repeat_add")
    }

    fun getMailboxExtractionColRef(): CollectionReference? {
        return getUserDocRef()?.collection("mailbox_extraction_mail_type_settings")
    }

    fun getMailboxExtractionParamsColRef(): CollectionReference? {
        return getUserDocRef()?.collection("mailbox_extraction_params")
    }

    fun getSettingsColRef(): CollectionReference? {
        return getUserDocRef()?.collection("settings")
    }

    /**
     * users/{userId}/settings/userPreferences
     * */
    fun getUserPreferencesDocRef(): DocumentReference? {
        return getSettingsColRef()?.document("user_preferences")
    }
}