package gaku.original.myapplication.data.FirestoreRepository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import gaku.original.myapplication.FirestoreReference
import gaku.original.myapplication.data.dataClass.MailAutoExtrInternalType

class MailAutoExtractionFirestoreRepository(
    private val firestoreReference: FirestoreReference
) {

    fun getMailAutoExtractionColRef(): CollectionReference? {
        return firestoreReference.getMailAutoExtractionColRef()
    }

    fun getMailAutoExtractionDocRef(type: MailAutoExtrInternalType): DocumentReference? {
        return firestoreReference.getMailAutoExtractionDocRef(type)
    }


}