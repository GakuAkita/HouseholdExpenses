package gaku.original.myapplication

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

    private val firestore = Firebase.firestore

    private val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    fun getUsersColRef(): CollectionReference {
        return firestore.collection("users")
    }

    /**
     * users/{userId}
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
        return getUserDocRef()?.collection("repeatAdd")
    }
}