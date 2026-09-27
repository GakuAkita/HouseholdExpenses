package gaku.original.myapplication.ui.screens.global.settingMenu.repeatAdd.editDialog

import androidx.test.ext.junit.runners.AndroidJUnit4
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.ExpenseTemplate
import gaku.original.myapplication.data.dataClass.RepeatAdd
import gaku.original.myapplication.data.dataClass.RepeatFrequency
import gaku.original.myapplication.data.firebaseReference.FirestoreUserReference
import gaku.original.myapplication.data.repository.FirebaseTestEnvironment
import gaku.original.myapplication.data.repository.appTimeZone.FakeAppTimeZoneRepository
import gaku.original.myapplication.data.repository.awaitValue
import gaku.original.myapplication.data.repository.category.FakeCategoryRepository
import gaku.original.myapplication.data.repository.deleteAll
import gaku.original.myapplication.data.repository.repeatAdd.RepeatAddRepositoryFirestore
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepeatAddEditViewModelTest {

    private lateinit var reference: FirestoreUserReference
    private lateinit var repository: RepeatAddRepositoryFirestore

    @Before
    fun setUp() {
        reference = FirebaseTestEnvironment.firestoreReference(FirebaseTestEnvironment.newTestUser())
        repository = RepeatAddRepositoryFirestore(reference = reference)
    }

    @After
    fun tearDown() = runBlocking {
        reference.deleteAll()
    }

    @Test
    fun save_withoutInitialRepeatAdd_addsNewRepeatAdd() = runBlocking<Unit> {
        val viewModel = newViewModel(initialRepeatAdd = null)
        viewModel.onAmountChange("8000")
        viewModel.onCategorySelected(sampleCategory)
        viewModel.onRepeatFrequencySelected(RepeatFrequency.EveryMonth())
        viewModel.onDayChange("25")
        viewModel.onHourChange("9")
        viewModel.onMinuteChange("30")

        viewModel.onSaveClick()
        val state = viewModel.uiState.awaitValue { it.isSaved }

        val added = state.newRepeatAdd!!
        assertEquals(mapOf(added.id to added), repository.getAllRepeatAdds())
        assertEquals(RepeatFrequency.EveryMonth(day = 25, hour = 9, minute = 30), added.frequencyInfo)
        assertEquals(ExpenseTemplate(amount = 8000L, category = sampleCategory), added.expense)
    }

    @Test
    fun save_withInitialRepeatAdd_updatesTheSameRepeatAdd() = runBlocking<Unit> {
        val existing = repository.addRepeatAdd(sampleRepeatAdd)
        val viewModel = newViewModel(initialRepeatAdd = existing)
        viewModel.onAmountChange("5000")

        viewModel.onSaveClick()
        val state = viewModel.uiState.awaitValue { it.isSaved }

        assertNull(state.newRepeatAdd)
        val expected = existing.copy(expense = existing.expense.copy(amount = 5000L))
        assertEquals(mapOf(existing.id to expected), repository.getAllRepeatAdds())
    }

    private fun newViewModel(initialRepeatAdd: RepeatAdd?) = RepeatAddEditViewModel(
        initialRepeatAdd = initialRepeatAdd,
        repeatAddRepository = repository,
        categoryRepository = FakeCategoryRepository(),
        appTimeZoneRepository = FakeAppTimeZoneRepository()
    )

    companion object {
        private val sampleCategory =
            Category(id = "category1", timestamp = 1_770_000_000_000L, name = "生活費", enabled = true)

        private val sampleRepeatAdd = RepeatAdd(
            id = "repeat1",
            timestamp = 1_780_000_000_000L,
            expense = ExpenseTemplate(amount = 8000L, category = sampleCategory, note = "monthly"),
            frequencyInfo = RepeatFrequency.EveryMonth(day = 25, hour = 9, minute = 30)
        )
    }
}
