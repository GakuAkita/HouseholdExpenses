package gaku.original.myapplication.data.repository.category

import com.google.firebase.firestore.QuerySnapshot
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.datasource.room.dao.CategoryDao
import gaku.original.myapplication.data.datasource.room.entity.toEntity
import gaku.original.myapplication.data.firebaseReference.FirestoreUserReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * Firestore is the source of truth.
 * Every snapshot that comes from the server is also saved to [categoryDao] as a backup,
 * and the backup is shown until Firestore has categories to show (e.g. when offline).
 */
class CategoryRepositoryFirestore(
    reference: FirestoreUserReference,
    private val userId: String,
    private val categoryDao: CategoryDao
) : CategoryRepository {

    private val categoryCollection = reference.categoryCollection

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _categories = MutableStateFlow<Map<String, Category>>(emptyMap())
    override val categories: StateFlow<Map<String, Category>>
        get() = _categories

    /* Once Firestore gives us categories, the backup must not overwrite them. */
    @Volatile
    private var receivedFirestoreCategories = false

    init {
        scope.launch {
            val backup = loadBackup()
            Timber.d("category backup=$backup")
            _categories.update { current -> if (receivedFirestoreCategories) current else backup }
        }
    }

    private val listenerRegistration =
        categoryCollection.addSnapshotListener { snapshots, exception ->
            if (exception != null) {
                // 後述
                return@addSnapshotListener
            }

            if (snapshots == null) return@addSnapshotListener

            /* An empty local cache (e.g. offline right after install) is not a reason to hide the backup. */
            if (snapshots.metadata.isFromCache && snapshots.isEmpty) return@addSnapshotListener

            val categories = snapshots.toCategories()
            Timber.d("categories=$categories")
            receivedFirestoreCategories = true
            _categories.value = categories

            if (!snapshots.metadata.isFromCache) {
                scope.launch { saveBackup(categories) }
            }
        }

    override suspend fun addCategory(category: Category): Category {
        Timber.d("addCategory: $category")
        categoryCollection.document(category.id).set(category.toDto()).await()
        return category
    }

    override suspend fun updateCategory(category: Category) {
        categoryCollection.document(category.id).set(category.toDto()).await()
    }

    override suspend fun deleteCategory(categoryId: String) {
        categoryCollection.document(categoryId).delete().await()
    }

    override suspend fun getAllCategories(): Map<String, Category> {
        val snapshot = try {
            categoryCollection.get().await()
        } catch (e: Exception) {
            Timber.w(e, "Failed to get categories. Use the backup instead.")
            return loadBackup()
        }

        if (snapshot.metadata.isFromCache) {
            return snapshot.toCategories().ifEmpty { loadBackup() }
        }

        return snapshot.toCategories().also { saveBackup(it) }
    }

    private fun QuerySnapshot.toCategories(): Map<String, Category> =
        documents
            .mapNotNull { document -> document.toObject(CategoryDto::class.java)?.toDomain() }
            .associateBy { it.id }

    private suspend fun loadBackup(): Map<String, Category> =
        categoryDao.getCategories(userId)
            .map { it.toDomain() }
            .associateBy { it.id }

    private suspend fun saveBackup(categories: Map<String, Category>) {
        categoryDao.replaceCategories(userId, categories.values.map { it.toEntity(userId) })
    }

    override fun close() {
        listenerRegistration.remove()
        scope.cancel()
    }
}
