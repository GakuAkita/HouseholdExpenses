package gaku.original.myapplication.data.firebaseReference

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import gaku.original.myapplication.domain.AppUser

/**
 * User associated reference
 */
class FirestoreUserReference(
    appUser: AppUser,
    firestore: FirebaseFirestore
) {
    val firestoreUser = firestore.collection("users").document(appUser.id!!)

    val expenseReference = firestoreUser.collection("expenses")

    val categoryReference = firestoreUser.collection("categories")

    val userPreferencesReference = firestoreUser.collection("settings").document("user_preferences")

    val
}

class RealtimeDbUserReference(
    appUser: AppUser,
    realtimeDb: FirebaseDatabase
) {
    val realtimeDbUser = realtimeDb.reference.child("users").child(appUser.id!!)

}
