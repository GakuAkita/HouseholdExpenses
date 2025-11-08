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

    private val firestore = if (BuildConfig.DEBUG) {
        Firebase.firestore.also {
            Log.d(className, "Using Firestore emulator")
            it.useEmulator("10.0.2.2", 5002)
        }
    } else {
        Firebase.firestore
    }

    private val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

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