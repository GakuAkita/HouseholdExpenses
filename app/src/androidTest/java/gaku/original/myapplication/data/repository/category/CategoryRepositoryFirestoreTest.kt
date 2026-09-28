package gaku.original.myapplication.data.repository.category

import androidx.test.ext.junit.runners.AndroidJUnit4
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.datasource.room.AppDatabase
import gaku.original.myapplication.data.datasource.room.entity.toEntity
import gaku.original.myapplication.data.firebaseReference.FirestoreUserReference
import gaku.original.myapplication.data.repository.FirebaseTestEnvironment
import gaku.original.myapplication.data.repository.awaitValue
import gaku.original.myapplication.data.repository.deleteAll
import gaku.original.myapplication.data.repository.inMemoryAppDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class CategoryRepositoryFirestoreTest {

    private lateinit var userId: String
    private lateinit var reference: FirestoreUserReference
    private lateinit var database: AppDatabase
    private lateinit var repository: CategoryRepositoryFirestore

    @Before
    fun setUp() {
        val appUser = FirebaseTestEnvironment.newTestUser()
        userId = appUser.id!!
        reference = FirebaseTestEnvironment.firestoreReference(appUser)
        database = inMemoryAppDatabase()
        /* The snapshot listener starts in the constructor. */
        repository = newRepository()
    }

    @After
    fun tearDown() = runBlocking {
        repository.close()
        database.close()
        reference.deleteAll()
    }

    private fun newRepository() = CategoryRepositoryFirestore(
        reference = reference,
        userId = userId,
        categoryDao = database.categoryDao()
    )

    @Test
    fun getAllCategories_returnsEmptyWhenNothingSaved() = runBlocking<Unit> {
        assertTrue(repository.getAllCategories().isEmpty())
    }

    @Test
    fun addCategory_savesCategoryWithGivenId() = runBlocking<Unit> {
        val category = sampleCategory()
        val added = repository.addCategory(category)

        assertEquals(category, added)
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

        repository.deleteCategory(added.id)

        assertFalse(repository.getAllCategories().containsKey(added.id))
        repository.categories.awaitValue { !it.containsKey(added.id) }
    }

    @Test
    fun getAllCategories_savesBackup() = runBlocking<Unit> {
        val added = repository.addCategory(sampleCategory())

        repository.getAllCategories()

        assertEquals(listOf(added), backup())
    }

    @Test
    fun categories_savesServerSnapshotToBackup() = runBlocking<Unit> {
        val added = repository.addCategory(sampleCategory())

        /* The listener saves the backup in the background, so poll for it. */
        withTimeout(10.seconds) {
            while (backup() != listOf(added)) delay(100)
        }
    }

    @Test
    fun offline_usesBackup() = runBlocking<Unit> {
        val saved = sampleCategory()
        database.categoryDao().insertCategories(listOf(saved.toEntity(userId)))

        FirebaseTestEnvironment.firestore.disableNetwork().await()
        try {
            val offlineRepository = newRepository()
            try {
                assertEquals(mapOf(saved.id to saved), offlineRepository.getAllCategories())
                offlineRepository.categories.awaitValue { it == mapOf(saved.id to saved) }
            } finally {
                offlineRepository.close()
            }
        } finally {
            FirebaseTestEnvironment.firestore.enableNetwork().await()
        }
    }

    private suspend fun backup(): List<Category> =
        database.categoryDao().getCategories(userId).map { it.toDomain() }

    private fun sampleCategory() = Category(
        id = UUID.randomUUID().toString(),
        timestamp = 1_770_000_000_000L,
        name = "食費",
        enabled = true
    )
}
