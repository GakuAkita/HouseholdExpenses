package gaku.original.myapplication

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.firestore
import gaku.original.myapplication.data.dataClass.MailboxExtractionCommon
import javax.inject.Inject

/* @TODO throwを出してしまうとアプリがクラッシュするらしい。したがって、nullのときの回避策を作る必要ある。*/
class FirestoreReference @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) {
    val className: String = this::class.simpleName ?: "UnableToGetClassName"

    private val firestore = Firebase.firestore

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
        return getUserDocRef()?.collection("mailbox_extraction")
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

    /**
     * users/{userId}/mailAutoExtraction 配下のDocReference
     */
    fun getMailboxExtractionInternalDocRef(instance: MailboxExtractionCommon): DocumentReference? {
        return getMailboxExtractionColRef()?.document(instance.documentName)
    }
}