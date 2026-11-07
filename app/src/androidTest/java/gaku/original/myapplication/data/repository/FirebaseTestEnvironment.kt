package gaku.original.myapplication.data.repository

import android.net.Uri
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import gaku.original.myapplication.BuildConfig
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.datasource.room.AppDatabase
import gaku.original.myapplication.data.firebaseReference.FirestoreUserReference
import gaku.original.myapplication.data.firebaseReference.RealtimeDbUserReference
import gaku.original.myapplication.di.appContainer.FirebaseEmulatorAppContainer
import gaku.original.myapplication.di.appContainer.REALTIME_DATABASE_URL
import gaku.original.myapplication.domain.AppUser
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import kotlin.concurrent.thread
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
 *
 * The tests don't sign in, so before the first test the emulator's security rules are replaced
 * with rules that allow everything. The rules stay open until the emulators restart.
 * The real rules are tested in functions/test/integration (databaseRules, firestoreRules).
 */
object FirebaseTestEnvironment {
    private val container: FirebaseEmulatorAppContainer by lazy {
        val app = InstrumentationRegistry.getInstrumentation()
            .targetContext.applicationContext as MyApplication
        val container = app.appContainer as? FirebaseEmulatorAppContainer
            ?: error("Repository tests must run against the Firebase emulator (debug build).")
        openEmulatorRules(projectId = container.firestore.app.options.projectId!!)
        container
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

private const val OPEN_FIRESTORE_RULES = """rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}"""

private const val OPEN_REALTIME_DB_RULES = """{"rules": {".read": true, ".write": true}}"""

/** Replaces the security rules in the running Firestore and Realtime Database emulators. */
private fun openEmulatorRules(projectId: String) {
    val host = BuildConfig.FIREBASE_EMULATOR_HOST
    /* The emulator names the database after the first part of the URL's host. */
    val namespace = Uri.parse(REALTIME_DATABASE_URL).host!!.substringBefore('.')
    val firestoreBody = JSONObject().put(
        "rules",
        JSONObject().put(
            "files",
            JSONArray().put(
                JSONObject()
                    .put("name", "firestore.rules")
                    .put("content", OPEN_FIRESTORE_RULES)
            )
        )
    )

    /* Android doesn't allow network calls on the main thread. */
    var failure: Throwable? = null
    thread {
        try {
            put(
                url = "http://$host:5002/emulator/v1/projects/$projectId:securityRules",
                body = firestoreBody.toString()
            )
            put(
                url = "http://$host:9000/.settings/rules.json?ns=$namespace",
                body = OPEN_REALTIME_DB_RULES,
                /* The emulator accepts "owner" as an admin token. */
                headers = mapOf("Authorization" to "Bearer owner")
            )
        } catch (e: Throwable) {
            failure = e
        }
    }.join()
    failure?.let { throw IllegalStateException("Couldn't open the emulator rules. Are the emulators running?", it) }
}

private fun put(url: String, body: String, headers: Map<String, String> = emptyMap()) {
    val connection = URL(url).openConnection() as HttpURLConnection
    try {
        connection.requestMethod = "PUT"
        connection.doOutput = true
        connection.connectTimeout = 5_000
        connection.readTimeout = 5_000
        connection.setRequestProperty("Content-Type", "application/json")
        headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
        connection.outputStream.use { it.write(body.toByteArray()) }
        val code = connection.responseCode
        check(code in 200..299) {
            "PUT $url failed with $code: ${connection.errorStream?.bufferedReader()?.readText()}"
        }
    } finally {
        connection.disconnect()
    }
}

/** A Room database that disappears when it is closed. Close it in @After. */
fun inMemoryAppDatabase(): AppDatabase = Room.inMemoryDatabaseBuilder(
    InstrumentationRegistry.getInstrumentation().targetContext,
    AppDatabase::class.java
).build()

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
