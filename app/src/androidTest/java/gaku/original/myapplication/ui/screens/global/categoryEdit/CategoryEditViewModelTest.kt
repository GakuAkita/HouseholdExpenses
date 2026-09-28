package gaku.original.myapplication.ui.screens.global.categoryEdit

import androidx.test.ext.junit.runners.AndroidJUnit4
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.datasource.room.AppDatabase
import gaku.original.myapplication.data.firebaseReference.FirestoreUserReference
import gaku.original.myapplication.data.repository.FirebaseTestEnvironment
import gaku.original.myapplication.data.repository.awaitValue
import gaku.original.myapplication.data.repository.category.CategoryRepositoryFirestore
import gaku.original.myapplication.data.repository.deleteAll
import gaku.original.myapplication.data.repository.inMemoryAppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CategoryEditViewModelTest {

    private lateinit var reference: FirestoreUserReference
    private lateinit var database: AppDatabase
    private lateinit var repository: CategoryRepositoryFirestore
    private lateinit var viewModel: CategoryEditViewModel

    @Before
    fun setUp() {
        val appUser = FirebaseTestEnvironment.newTestUser()
        reference = FirebaseTestEnvironment.firestoreReference(appUser)
        database = inMemoryAppDatabase()
        repository = CategoryRepositoryFirestore(
            reference = reference,
            userId = appUser.id!!,
            categoryDao = database.categoryDao()
        )
        viewModel = CategoryEditViewModel(repository)
    }

    @After
    fun tearDown() = runBlocking {
        repository.close()
        database.close()
        reference.deleteAll()
    }

    @Test
    fun save_afterAddClick_addsEnabledCategory() = runBlocking<Unit> {
        viewModel.onCategoryAddClick()
        viewModel.onSave("食費")

        val categories = repository.categories.awaitValue { it.isNotEmpty() }
        val added = categories.values.single()
        assertEquals("食費", added.name)
        assertTrue(added.enabled)
        assertEquals(added.id, categories.keys.single())
    }

    @Test
    fun save_afterSelectingCategory_renamesTheSameCategory() = runBlocking<Unit> {
        val existing = repository.addCategory(
            Category(id = "category1", timestamp = 1_770_000_000_000L, name = "食費", enabled = false)
        )
        repository.categories.awaitValue { it.containsKey(existing.id) }

        viewModel.onCategorySelected(existing)
        viewModel.onSave("交通費")

        val categories = repository.categories.awaitValue { it[existing.id]?.name == "交通費" }
        assertEquals(mapOf(existing.id to existing.copy(name = "交通費")), categories)
    }
}
