package gaku.original.myapplication.data.repository.appTimeZone

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.firestore.FirebaseFirestoreException
import gaku.original.myapplication.common.CodingErrorException
import gaku.original.myapplication.data.firebaseReference.FirestoreUserReference
import gaku.original.myapplication.data.repository.FirebaseTestEnvironment
import gaku.original.myapplication.data.repository.assertThrowsSuspend
import gaku.original.myapplication.data.repository.awaitValue
import gaku.original.myapplication.data.repository.deleteAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.ZoneId

@RunWith(AndroidJUnit4::class)
class AppTimeZoneRepositoryFirestoreTest {

    private lateinit var reference: FirestoreUserReference
    private lateinit var repository: AppTimeZoneRepositoryFirestore

    @Before
    fun setUp() {
        reference = FirebaseTestEnvironment.firestoreReference(FirebaseTestEnvironment.newTestUser())
        repository = AppTimeZoneRepositoryFirestore(reference = reference)
    }

    @After
    fun tearDown() = runBlocking {
        repository.stopListening()
        reference.deleteAll()
    }

    @Test
    fun getZoneId_returnsSystemDefaultWhenNotSaved() = runBlocking<Unit> {
        assertEquals(ZoneId.systemDefault(), repository.getZoneId(ZoneId.systemDefault()))
    }

    @Test
    fun getZoneId_returnsSavedZone() = runBlocking<Unit> {
        savePreferences(timeZone = "America/New_York")

        assertEquals(ZoneId.of("America/New_York"), repository.getZoneId(ZoneId.systemDefault()))
    }

    @Test
    fun updateZoneId_updatesSavedZone() = runBlocking<Unit> {
        savePreferences(timeZone = "Asia/Tokyo")

        repository.updateZoneId(ZoneId.of("Europe/London"))

        assertEquals(ZoneId.of("Europe/London"), repository.getZoneId(ZoneId.systemDefault()))
    }

    @Test
    fun updateZoneId_keepsOtherPreferences() = runBlocking<Unit> {
        savePreferences(timeZone = "Asia/Tokyo")

        repository.updateZoneId(ZoneId.of("Europe/London"))

        val snapshot = reference.userPreferencesDocument.get().await()
        assertEquals("other", snapshot.getString("otherPreference"))
    }

    @Test
    fun updateZoneId_failsWhenPreferencesDocumentDoesNotExist() = runBlocking<Unit> {
        /* update() requires an existing document. The document is expected to be created on sign up. */
        val exception = assertThrowsSuspend<FirebaseFirestoreException> {
            repository.updateZoneId(ZoneId.of("Europe/London"))
        }
        assertEquals(FirebaseFirestoreException.Code.NOT_FOUND, exception.code)
    }

    @Test
    fun startListening_emitsSavedAndUpdatedZone() = runBlocking<Unit> {
        val initial = pickZoneOtherThanSystemDefault("Asia/Tokyo", "America/New_York")
        val updated = pickZoneOtherThanSystemDefault("Europe/London", "Australia/Sydney")
        savePreferences(timeZone = initial.id)

        repository.startListening()
        repository.zoneId.awaitValue { it == initial }

        repository.updateZoneId(updated)
        repository.zoneId.awaitValue { it == updated }
    }

    @Test
    fun startListening_twiceThrows() {
        repository.startListening()

        assertThrows(CodingErrorException::class.java) {
            repository.startListening()
        }
    }

    private suspend fun savePreferences(timeZone: String) {
        reference.userPreferencesDocument.set(
            mapOf(
                "timeZone" to timeZone,
                "otherPreference" to "other"
            )
        ).await()
    }

    /* zoneId starts with systemDefault, so the expected zone must differ from it. */
    private fun pickZoneOtherThanSystemDefault(first: String, second: String): ZoneId {
        val zone = ZoneId.of(first)
        return if (zone != ZoneId.systemDefault()) zone else ZoneId.of(second)
    }
}
