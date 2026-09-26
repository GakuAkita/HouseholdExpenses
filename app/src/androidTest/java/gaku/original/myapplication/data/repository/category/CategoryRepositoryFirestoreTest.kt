package gaku.original.myapplication.data.repository.category

import androidx.test.ext.junit.runners.AndroidJUnit4
import gaku.original.myapplication.data.dataClass.Category
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

@RunWith(AndroidJUnit4::class)
class CategoryRepositoryFirestoreTest {

    private lateinit var reference: FirestoreUserReference
    private lateinit var repository: CategoryRepositoryFirestore

    @Before
    fun setUp() {
        reference = FirebaseTestEnvironment.firestoreReference(FirebaseTestEnvironment.newTestUser())
        /* The snapshot listener starts in the constructor. */
        repository = CategoryRepositoryFirestore(reference = reference)
    }

    @After
    fun tearDown() = runBlocking {
        repository.close()
        reference.deleteAll()
    }

    @Test
    fun getAllCategories_returnsEmptyWhenNothingSaved() = runBlocking<Unit> {
        assertTrue(repository.getAllCategories().isEmpty())
    }

    @Test
    fun addCategory_assignsIdAndSavesCategory() = runBlocking<Unit> {
        val added = repository.addCategory(sampleCategory())

        assertNotNull(added.id)
        assertEquals(mapOf(added.id to added), repository.getAllCategories())
    }

    @Test
    fun categories_reflectsAddedCategory() = runBlocking<Unit> {
        val added = repository.addCategory(sampleCategory())

        val categories = repository.categories.awaitValue { it.containsKey(added.id) }
        assertEquals(added, categories[added.id])
    }

    @Test
    fun updateCategory_overwritesExistingCategory() = runBlocking<Unit> {
        val added = repository.addCategory(sampleCategory())
        val modified = added.copy(name = "交通費", enabled = false)

        repository.updateCategory(modified)

        assertEquals(modified, repository.getAllCategories()[added.id])
        repository.categories.awaitValue { it[added.id] == modified }
    }

    @Test
    fun deleteCategory_removesCategory() = runBlocking<Unit> {
        val added = repository.addCategory(sampleCategory())
        repository.categories.awaitValue { it.containsKey(added.id) }

        repository.deleteCategory(added.id!!)

        assertFalse(repository.getAllCategories().containsKey(added.id))
        repository.categories.awaitValue { !it.containsKey(added.id) }
    }

    private fun sampleCategory() = Category(
        timestamp = 1_770_000_000_000L,
        name = "食費",
        enabled = true
    )
}
