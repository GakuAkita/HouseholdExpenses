package gaku.original.myapplication.data.repository.expense

import com.google.firebase.firestore.FirebaseFirestore
import gaku.original.myapplication.data.firebaseReference.FirestoreUserReference
import gaku.original.myapplication.domain.AppUser
import org.junit.jupiter.api.BeforeEach

class ExpenseRepositoryFirestoreTest {

    lateinit var reference: FirestoreUserReference
    lateinit var repository: ExpenseRepositoryFirestore

    @BeforeEach
    fun setUp() {
        val firestore = FirebaseFirestore.getInstance()
        firestore.useEmulator("10.0.2.2", 8080)

        reference = FirestoreUserReference(
            appUser = AppUser(id = "testUserId"),
            firestore = firestore
        )

        repository = ExpenseRepositoryFirestore(
            reference = reference
        )
    }

}