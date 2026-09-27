package gaku.original.myapplication.ui.screens.global.expenseAddEdit

import androidx.test.ext.junit.runners.AndroidJUnit4
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.GeneratedType
import gaku.original.myapplication.data.firebaseReference.FirestoreUserReference
import gaku.original.myapplication.data.repository.FirebaseTestEnvironment
import gaku.original.myapplication.data.repository.appTimeZone.FakeAppTimeZoneRepository
import gaku.original.myapplication.data.repository.awaitValue
import gaku.original.myapplication.data.repository.category.FakeCategoryRepository
import gaku.original.myapplication.data.repository.deleteAll
import gaku.original.myapplication.data.repository.expense.ExpenseDto
import gaku.original.myapplication.data.repository.expense.ExpenseRepositoryFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExpenseAddEditViewModelTest {

    private lateinit var reference: FirestoreUserReference
    private lateinit var repository: ExpenseRepositoryFirestore

    @Before
    fun setUp() {
        reference = FirebaseTestEnvironment.firestoreReference(FirebaseTestEnvironment.newTestUser())
        repository = ExpenseRepositoryFirestore(reference = reference)
    }

    @After
    fun tearDown() = runBlocking {
        reference.deleteAll()
    }

    @Test
    fun save_inEditMode_updatesTheSameDocument() = runBlocking<Unit> {
        val added = repository.addExpense(sampleExpense)
        val viewModel = ExpenseAddEditViewModel(
            ExpenseAddEditMode.Edit(added),
            repository,
            FakeAppTimeZoneRepository(),
            FakeCategoryRepository()
        )

        viewModel.onSaveClick()
        viewModel.uiState.awaitValue { it.isSaveDone }

        val documents = reference.expenseCollection.get().await().documents
        assertEquals(listOf(added.id), documents.map { it.id })
        assertEquals(added, documents.single().toObject(ExpenseDto::class.java)!!.toDomain())
    }

    companion object {
        private val sampleExpense = Expense(
            generatedType = GeneratedType.Manual,
            datetime = "2026-09-15T03:00:00Z",
            timestamp = 1_780_000_000_000L,
            amount = 1200L,
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
    }
}
