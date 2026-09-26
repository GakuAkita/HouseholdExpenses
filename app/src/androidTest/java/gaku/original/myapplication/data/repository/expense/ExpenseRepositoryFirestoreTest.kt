package gaku.original.myapplication.data.repository.expense

import androidx.test.ext.junit.runners.AndroidJUnit4
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.GeneratedType
import gaku.original.myapplication.data.firebaseReference.FirestoreUserReference
import gaku.original.myapplication.data.repository.FirebaseTestEnvironment
import gaku.original.myapplication.data.repository.awaitValue
import gaku.original.myapplication.data.repository.deleteAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class ExpenseRepositoryFirestoreTest {

    private lateinit var reference: FirestoreUserReference
    private lateinit var repository: ExpenseRepositoryFirestore

    @Before
    fun setUp() {
        reference = FirebaseTestEnvironment.firestoreReference(FirebaseTestEnvironment.newTestUser())
        repository = ExpenseRepositoryFirestore(reference = reference)
    }

    @After
    fun tearDown() = runBlocking {
        repository.stopListening(SUBSCRIPTION_ID)
        repository.stopListening(OTHER_SUBSCRIPTION_ID)
        reference.deleteAll()
    }

    @Test
    fun addExpense_assignsIdAndSavesAllFields() = runBlocking<Unit> {
        val added = repository.addExpense(sampleExpense())

        assertNotNull(added.id)
        val saved = reference.expenseCollection.document(added.id!!).get().await().toExpense()
        assertEquals(added, saved)
    }

    @Test
    fun addExpense_savesGeneratedTypeWithParameter() = runBlocking<Unit> {
        val added = repository.addExpense(
            sampleExpense().copy(generatedType = GeneratedType.RepeatAdd(repeatAddId = "repeat1"))
        )

        val saved = reference.expenseCollection.document(added.id!!).get().await().toExpense()
        assertEquals(GeneratedType.RepeatAdd(repeatAddId = "repeat1"), saved.generatedType)
    }

    @Test
    fun updateExpense_overwritesExistingDocument() = runBlocking<Unit> {
        val added = repository.addExpense(sampleExpense())
        val modified = added.copy(amount = 9999L, note = "updated", category = null)

        repository.updateExpense(modified)

        val saved = reference.expenseCollection.document(added.id!!).get().await().toExpense()
        assertEquals(modified, saved)
    }

    @Test
    fun removeExpense_deletesDocument() = runBlocking<Unit> {
        val added = repository.addExpense(sampleExpense())

        repository.removeExpense(added.id!!)

        val snapshot = reference.expenseCollection.document(added.id!!).get().await()
        assertFalse(snapshot.exists())
    }

    @Test
    fun startListening_receivesExistingAndNewExpenses() = runBlocking<Unit> {
        val existing = repository.addExpense(sampleExpense())
        repository.startListening(SUBSCRIPTION_ID, ExpenseQuery())

        repository.expenses.awaitValue { it[SUBSCRIPTION_ID]?.containsKey(existing.id) == true }

        val added = repository.addExpense(sampleExpense(amount = 500L))
        val expenses = repository.expenses.awaitValue {
            it[SUBSCRIPTION_ID]?.containsKey(added.id) == true
        }
        assertEquals(existing, expenses[SUBSCRIPTION_ID]!![existing.id])
        assertEquals(added, expenses[SUBSCRIPTION_ID]!![added.id])
    }

    @Test
    fun startListening_reflectsModificationAndRemoval() = runBlocking<Unit> {
        val added = repository.addExpense(sampleExpense())
        repository.startListening(SUBSCRIPTION_ID, ExpenseQuery())
        repository.expenses.awaitValue { it[SUBSCRIPTION_ID]?.containsKey(added.id) == true }

        val modified = repository.updateExpense(added.copy(amount = 3000L))
        repository.expenses.awaitValue { it[SUBSCRIPTION_ID]?.get(added.id) == modified }

        repository.removeExpense(added.id!!)
        repository.expenses.awaitValue { it[SUBSCRIPTION_ID]?.containsKey(added.id) == false }
    }

    @Test
    fun startListening_filtersByDatetimeRange() = runBlocking<Unit> {
        val before = repository.addExpense(sampleExpense(datetime = "2026-08-31T23:59:59Z"))
        val atStart = repository.addExpense(sampleExpense(datetime = "2026-09-01T00:00:00Z"))
        val inside = repository.addExpense(sampleExpense(datetime = "2026-09-15T12:00:00Z"))
        val atEnd = repository.addExpense(sampleExpense(datetime = "2026-10-01T00:00:00Z"))

        repository.startListening(
            SUBSCRIPTION_ID,
            ExpenseQuery(
                datetimeFromOrEqual = Instant.parse("2026-09-01T00:00:00Z"),
                datetimeTo = Instant.parse("2026-10-01T00:00:00Z")
            )
        )

        val expenses = repository.expenses.awaitValue {
            it[SUBSCRIPTION_ID]?.keys?.containsAll(listOf(atStart.id, inside.id)) == true
        }[SUBSCRIPTION_ID]!!
        /* start is inclusive, end is exclusive */
        assertEquals(setOf(atStart.id, inside.id), expenses.keys)
        assertFalse(expenses.containsKey(before.id))
        assertFalse(expenses.containsKey(atEnd.id))
    }

    @Test
    fun subscriptionsAreIndependent() = runBlocking<Unit> {
        val september = repository.addExpense(sampleExpense(datetime = "2026-09-10T00:00:00Z"))
        val october = repository.addExpense(sampleExpense(datetime = "2026-10-10T00:00:00Z"))

        repository.startListening(
            SUBSCRIPTION_ID,
            ExpenseQuery(
                datetimeFromOrEqual = Instant.parse("2026-09-01T00:00:00Z"),
                datetimeTo = Instant.parse("2026-10-01T00:00:00Z")
            )
        )
        repository.startListening(
            OTHER_SUBSCRIPTION_ID,
            ExpenseQuery(
                datetimeFromOrEqual = Instant.parse("2026-10-01T00:00:00Z"),
                datetimeTo = Instant.parse("2026-11-01T00:00:00Z")
            )
        )

        val expenses = repository.expenses.awaitValue {
            it[SUBSCRIPTION_ID]?.containsKey(september.id) == true &&
                    it[OTHER_SUBSCRIPTION_ID]?.containsKey(october.id) == true
        }
        assertEquals(setOf(september.id), expenses[SUBSCRIPTION_ID]!!.keys)
        assertEquals(setOf(october.id), expenses[OTHER_SUBSCRIPTION_ID]!!.keys)
    }

    @Test
    fun stopListening_removesSubscriptionFromExpenses() = runBlocking<Unit> {
        val added = repository.addExpense(sampleExpense())
        repository.startListening(SUBSCRIPTION_ID, ExpenseQuery())
        repository.expenses.awaitValue { it[SUBSCRIPTION_ID]?.containsKey(added.id) == true }

        repository.stopListening(SUBSCRIPTION_ID)

        assertFalse(repository.expenses.value.containsKey(SUBSCRIPTION_ID))
    }

    private fun sampleExpense(
        datetime: String = "2026-09-15T03:00:00Z",
        amount: Long = 1200L
    ) = Expense(
        generatedType = GeneratedType.Manual,
        datetime = datetime,
        timestamp = 1_780_000_000_000L,
        amount = amount,
        category = Category(
            id = "category1",
            timestamp = 1_770_000_000_000L,
            name = "食費",
            enabled = true
        ),
        note = "note",
        storeName = "store",
        itemName = "item"
    )

    companion object {
        private const val SUBSCRIPTION_ID = "subscription"
        private const val OTHER_SUBSCRIPTION_ID = "other_subscription"
    }
}
