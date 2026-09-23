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

    val expenseCollection = firestoreUser.collection("expenses")

    val categoryCollection = firestoreUser.collection("categories")

    val settingsCollection = firestoreUser.collection("settings")
    val userPreferencesDocument = settingsCollection.document("user_preferences")

    val repeatAddCollection = firestoreUser.collection("repeat_add")
}

class RealtimeDbUserReference(
    appUser: AppUser,
    realtimeDb: FirebaseDatabase
) {
    val realtimeDbUser = realtimeDb.reference.child("users").child(appUser.id!!)

    val mailboxExtractionReference = realtimeDbUser.child("mailbox_extraction")

    val emailTemplateSettingsReference = mailboxExtractionReference.child("email_template_settings")

    val gmailTokensReference = mailboxExtractionReference.child("gmail_tokens")

    val amazonSubscribeMonitorReference =
        mailboxExtractionReference.child("amazon_subscribe_monitor")

    val amazonSubscribeItemReference = amazonSubscribeMonitorReference.child("subscribe_items")
    val amazonSubscribeLastExecReference = amazonSubscribeMonitorReference.child("last_exec")

    val categoryAssignmentReference = realtimeDbUser.child("category_assignment_data")
}
