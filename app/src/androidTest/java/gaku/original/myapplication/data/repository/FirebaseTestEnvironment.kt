package gaku.original.myapplication.data.repository

import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.firebaseReference.FirestoreUserReference
import gaku.original.myapplication.data.firebaseReference.RealtimeDbUserReference
import gaku.original.myapplication.di.appContainer.FirebaseEmulatorAppContainer
import gaku.original.myapplication.domain.AppUser
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Repository tests talk to the Firebase Local Emulator.
 *
 * MyApplication already calls useEmulator() on the default instances in debug builds,
 * and useEmulator() cannot be called again once an instance is in use.
 * So the tests reuse the instances owned by FirebaseEmulatorAppContainer.
 *
 * Before running: start the emulators (start_emulator.bat) and run `connectedDebugAndroidTest`.
 */
object FirebaseTestEnvironment {
    private val container: FirebaseEmulatorAppContainer by lazy {
        val app = InstrumentationRegistry.getInstrumentation()
            .targetContext.applicationContext as MyApplication
        app.appContainer as? FirebaseEmulatorAppContainer
            ?: error("Repository tests must run against the Firebase emulator (debug build).")
    }

    val firestore: FirebaseFirestore get() = container.firestore

    val realtimeDb: FirebaseDatabase get() = container.firebaseRealtimeDb

    /* A unique user per test keeps tests independent from each other and from manual data. */
    fun newTestUser(): AppUser = AppUser(id = "test_${UUID.randomUUID()}")

    fun firestoreReference(appUser: AppUser) = FirestoreUserReference(
        appUser = appUser,
        firestore = firestore
    )

    fun realtimeDbReference(appUser: AppUser) = RealtimeDbUserReference(
        appUser = appUser,
        realtimeDb = realtimeDb
    )
}

/* Firestore does not delete sub collections together with the parent document. */
suspend fun FirestoreUserReference.deleteAll() {
    listOf(
        expenseCollection,
        categoryCollection,
        settingsCollection,
        repeatAddCollection
    ).forEach { it.deleteAllDocuments() }
    firestoreUser.delete().await()
}

private suspend fun CollectionReference.deleteAllDocuments() {
    get().await().documents.forEach { it.reference.delete().await() }
}

suspend fun RealtimeDbUserReference.deleteAll() {
    realtimeDbUser.removeValue().await()
}

/** Waits until a snapshot listener pushes a value that satisfies [predicate]. */
suspend fun <T> StateFlow<T>.awaitValue(
    timeout: Duration = 10.seconds,
    predicate: (T) -> Boolean
): T = withTimeout(timeout) { first(predicate) }

/** assertThrows for suspend functions. */
suspend inline fun <reified E : Throwable> assertThrowsSuspend(block: () -> Unit): E {
    try {
        block()
    } catch (e: Throwable) {
        if (e is E) return e
        throw AssertionError("Expected ${E::class.simpleName} but got ${e::class.simpleName}", e)
    }
    throw AssertionError("Expected ${E::class.simpleName} but nothing was thrown")
}
