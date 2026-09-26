package gaku.original.myapplication.data.repository.repeatAdd

import androidx.test.ext.junit.runners.AndroidJUnit4
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.RepeatAdd
import gaku.original.myapplication.data.dataClass.RepeatFrequency
import gaku.original.myapplication.data.firebaseReference.FirestoreUserReference
import gaku.original.myapplication.data.repository.FirebaseTestEnvironment
import gaku.original.myapplication.data.repository.awaitValue
import gaku.original.myapplication.data.repository.deleteAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.DayOfWeek

@RunWith(AndroidJUnit4::class)
class RepeatAddRepositoryFirestoreTest {

    private lateinit var reference: FirestoreUserReference
    private lateinit var repository: RepeatAddRepositoryFirestore

    @Before
    fun setUp() {
        reference = FirebaseTestEnvironment.firestoreReference(FirebaseTestEnvironment.newTestUser())
        repository = RepeatAddRepositoryFirestore(reference = reference)
    }

    @After
    fun tearDown() = runBlocking {
        repository.stopListening()
        reference.deleteAll()
    }

    @Test
    fun getAllRepeatAdds_returnsEmptyWhenNothingSaved() = runBlocking<Unit> {
        assertTrue(repository.getAllRepeatAdds().isEmpty())
    }

    @Test
    fun addRepeatAdd_assignsIdAndTimestamp() = runBlocking<Unit> {
        val before = System.currentTimeMillis()

        val added = repository.addRepeatAdd(sampleRepeatAdd())

        assertNotNull(added.id)
        assertTrue(added.timestamp!! >= before)
    }

    @Test
    fun addRepeatAdd_savesExpenseAndFrequency() = runBlocking<Unit> {
        val added = repository.addRepeatAdd(sampleRepeatAdd())

        val saved = repository.getAllRepeatAdds()[added.id]!!
        assertEquals(added.id, saved.id)
        assertEquals(added.timestamp, saved.timestamp)
        assertEquals(added.frequencyInfo, saved.frequencyInfo)
        assertSameExpenseTemplate(added.expense, saved.expense)
    }

    @Test
    fun addRepeatAdd_savesEveryFrequencyType() = runBlocking<Unit> {
        val frequencies = listOf(
            RepeatFrequency.EveryYear(month = 12, day = 31, hour = 23, minute = 59),
            RepeatFrequency.EveryMonth(day = 25, hour = 9, minute = 30),
            RepeatFrequency.EveryWeek(
                dayOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
                hour = 8,
                minute = 0
            ),
            RepeatFrequency.Weekdays(hour = 7, minute = 15),
            RepeatFrequency.Weekends(hour = 10, minute = 45),
            RepeatFrequency.Everyday(hour = 6, minute = 0),
        )

        val added = frequencies.map {
            repository.addRepeatAdd(sampleRepeatAdd().copy(frequencyInfo = it))
        }

        val saved = repository.getAllRepeatAdds()
        added.forEach { assertEquals(it.frequencyInfo, saved[it.id]!!.frequencyInfo) }
    }

    @Test
    fun updateRepeatAdd_overwritesExistingRepeatAdd() = runBlocking<Unit> {
        val added = repository.addRepeatAdd(sampleRepeatAdd())
        val modified = added.copy(
            expense = added.expense.copy(amount = 5000L, note = "updated"),
            frequencyInfo = RepeatFrequency.Everyday(hour = 1, minute = 2)
        )

        repository.updateRepeatAdd(modified)

        val saved = repository.getAllRepeatAdds()[added.id]!!
        assertEquals(modified.frequencyInfo, saved.frequencyInfo)
        assertSameExpenseTemplate(modified.expense, saved.expense)
    }

    @Test
    fun deleteRepeatAdd_removesRepeatAdd() = runBlocking<Unit> {
        val added = repository.addRepeatAdd(sampleRepeatAdd())

        repository.deleteRepeatAdd(added.id!!)

        assertFalse(repository.getAllRepeatAdds().containsKey(added.id))
    }

    @Test
    fun startListening_reflectsAddAndDelete() = runBlocking<Unit> {
        repository.startListening()

        val added = repository.addRepeatAdd(sampleRepeatAdd())
        val repeatAdds = repository.repeatAdds.awaitValue { it.containsKey(added.id) }
        assertEquals(added.frequencyInfo, repeatAdds[added.id]!!.frequencyInfo)

        repository.deleteRepeatAdd(added.id!!)
        repository.repeatAdds.awaitValue { !it.containsKey(added.id) }
    }

    /* Only these fields are restored for the expense template of RepeatAdd. */
    private fun assertSameExpenseTemplate(expected: Expense, actual: Expense) {
        assertEquals(expected.category, actual.category)
        assertEquals(expected.amount, actual.amount)
        assertEquals(expected.storeName, actual.storeName)
        assertEquals(expected.itemName, actual.itemName)
        assertEquals(expected.note, actual.note)
    }

    private fun sampleRepeatAdd() = RepeatAdd(
        expense = Expense(
            amount = 8000L,
            category = Category(
                id = "category1",
                timestamp = 1_770_000_000_000L,
                name = "生活費",
                enabled = true
            ),
            storeName = "store",
            itemName = "rent",
            note = "monthly"
        ),
        frequencyInfo = RepeatFrequency.EveryMonth(day = 25, hour = 9, minute = 30)
    )
}
