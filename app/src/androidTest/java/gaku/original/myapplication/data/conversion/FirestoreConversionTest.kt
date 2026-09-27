package gaku.original.myapplication.data.conversion

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.ExpenseTemplate
import gaku.original.myapplication.data.dataClass.GeneratedType
import gaku.original.myapplication.data.dataClass.RepeatAdd
import gaku.original.myapplication.data.dataClass.RepeatFrequency
import gaku.original.myapplication.data.firebaseReference.FirestoreUserReference
import gaku.original.myapplication.data.repository.FirebaseTestEnvironment
import gaku.original.myapplication.data.repository.category.CategoryDto
import gaku.original.myapplication.data.repository.category.toDto
import gaku.original.myapplication.data.repository.deleteAll
import gaku.original.myapplication.data.repository.expense.ExpenseDto
import gaku.original.myapplication.data.repository.expense.toDto
import gaku.original.myapplication.data.repository.repeatAdd.toFirestore
import gaku.original.myapplication.data.repository.repeatAdd.toRepeatAdd
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.DayOfWeek
import java.time.Instant

/**
 * Saves each data class to the Firestore emulator and reads it back with the app's conversion code.
 *
 * When you add a property to a data class, a "samples are fully populated" test fails first.
 * Set the new property in the sample below, then the round trip tests check its conversion.
 * When you add a subtype to a sealed type, add a sample for it in the same way.
 */
@RunWith(AndroidJUnit4::class)
class FirestoreConversionTest {

    private lateinit var reference: FirestoreUserReference

    @Before
    fun setUp() {
        reference =
            FirebaseTestEnvironment.firestoreReference(FirebaseTestEnvironment.newTestUser())
    }

    @After
    fun tearDown() = runBlocking {
        reference.deleteAll()
    }

    /* ---------- Category ---------- */

    @Test
    fun category_sampleIsFullyPopulated() {
        /* Even if you forget to put the value for the newly added property, this test can catch it. */
        /* Don't worry. */
        assertFullyPopulated(sampleCategory)
        /* Fails when a property of CategoryDto is not set by toDto(). */
        assertFullyPopulated(sampleCategory.toDto())
    }

    @Test
    fun category_roundTrip() = runBlocking<Unit> {
        val snapshot = reference.categoryCollection.saveAndLoad(sampleCategory.toDto())

        val actual = snapshot.toObject(CategoryDto::class.java)!!.toDomain()

        assertSameProperties(sampleCategory, actual)
        assertEquals(sampleCategory, actual)
    }

    /* ---------- Expense ---------- */

    @Test
    fun expense_samplesAreFullyPopulated() {
        assertCoversAllSubclasses(GeneratedType::class, generatedTypeSamples)
        generatedTypeSamples.forEach { assertFullyPopulated(it) }
        expenseSamples.forEach { assertFullyPopulated(it) }
        /* Fails when a property of ExpenseDto is not set by toDto(). */
        expenseSamples.forEach { assertFullyPopulated(it.toDto()) }
    }

    @Test
    fun expense_roundTrip() = runBlocking<Unit> {
        expenseSamples.forEach { expected ->
            val snapshot = reference.expenseCollection.saveAndLoad(expected.toDto())

            val actual = snapshot.toObject(ExpenseDto::class.java)!!.toDomain()

            assertSameProperties(expected, actual)
            assertEquals(expected, actual)
        }
    }

    /* ---------- RepeatAdd ---------- */

    @Test
    fun repeatAdd_samplesAreFullyPopulated() {
        assertCoversAllSubclasses(RepeatFrequency::class, repeatFrequencySamples)
        repeatFrequencySamples.forEach { assertFullyPopulated(it) }
        assertFullyPopulated(sampleExpenseTemplate)
        repeatAddSamples.forEach { assertFullyPopulated(it) }
    }

    @Test
    fun repeatAdd_documentSnapshotRoundTrip() = runBlocking<Unit> {
        repeatAddSamples.forEach { expected ->
            val snapshot = reference.repeatAddCollection.saveAndLoad(expected.toFirestore())

            val actual = snapshot.toRepeatAdd()

            assertSameProperties(expected, actual)
            assertSameProperties(expected.expense, actual.expense)
            assertEquals(expected, actual)
        }
    }

    private suspend fun CollectionReference.saveAndLoad(data: Any): DocumentSnapshot {
        val document = document()
        document.set(data).await()
        return document.get().await()
    }

    companion object {
        private val sampleCategory = Category(
            id = "category1",
            timestamp = 1_770_000_000_000L,
            name = "食費",
            enabled = false
        )

        private val generatedTypeSamples = listOf(
            GeneratedType.Manual,
            GeneratedType.RepeatAdd(repeatAddId = "repeat1"),
            GeneratedType.MailExtraction(templateTypeName = "amazon_item"),
        )

        private val expenseSamples = generatedTypeSamples.map { generatedType ->
            Expense(
                id = "expense1",
                generatedType = generatedType,
                datetime = Instant.parse("2026-09-15T03:00:00Z"),
                timestamp = 1_780_000_000_000L,
                amount = 1200L,
                category = sampleCategory,
                note = "note",
                storeName = "store",
                itemName = "item"
            )
        }

        private val repeatFrequencySamples = listOf(
            RepeatFrequency.EveryYear(month = 12, day = 31, hour = 23, minute = 59),
            RepeatFrequency.EveryMonth(day = 25, hour = 9, minute = 30),
            RepeatFrequency.EveryWeek(
                dayOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
                hour = 8,
                minute = 15
            ),
            RepeatFrequency.Weekdays(hour = 7, minute = 15),
            RepeatFrequency.Weekends(hour = 10, minute = 45),
            RepeatFrequency.Everyday(hour = 6, minute = 5),
        )

        private val sampleExpenseTemplate = ExpenseTemplate(
            amount = 8000L,
            category = sampleCategory,
            note = "note",
            storeName = "store",
            itemName = "item"
        )

        private val repeatAddSamples = repeatFrequencySamples.map { frequency ->
            RepeatAdd(
                id = "repeat1",
                timestamp = 1_780_000_000_000L,
                expense = sampleExpenseTemplate,
                frequencyInfo = frequency
            )
        }
    }
}
