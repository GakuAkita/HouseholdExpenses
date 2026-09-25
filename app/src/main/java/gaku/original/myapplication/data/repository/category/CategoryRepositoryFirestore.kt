package gaku.original.myapplication.data.repository.category

import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.firebaseReference.FirestoreUserReference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class CategoryRepositoryFirestore(
    reference: FirestoreUserReference
) : CategoryRepository {

    private val categoryCollection = reference.categoryCollection

    private val _categories = MutableStateFlow<Map<String, Category>>(emptyMap())
    override val categories: StateFlow<Map<String, Category>>
        get() = _categories

    private val listenerRegistration =
        categoryCollection.addSnapshotListener { snapshots, exception ->
            if (exception != null) {
                // 後述
                return@addSnapshotListener
            }

            if (snapshots == null) return@addSnapshotListener

            val categories = snapshots.documents
                .mapNotNull { document ->
                    Timber.d("document=$document")
                    document.data?.toCategory()
                }
                .associateBy { it.id!! }

            Timber.d("categories=$categories")
            _categories.value = categories
        }

    override suspend fun addCategory(category: Category): Category {
        Timber.d("addCategory called")
        val newId = categoryCollection.document().id
        Timber.d("newId=$newId")
        val newCategory = category.copy(id = newId)
        categoryCollection.document(newId).set(newCategory).await()
        return newCategory
    }

    override suspend fun updateCategory(category: Category) {
        categoryCollection.document(category.id!!).set(category).await()
    }

    override suspend fun deleteCategory(categoryId: String) {
        categoryCollection.document(categoryId).delete().await()
    }

    override suspend fun getAllCategories(): Map<String, Category> {
        val snapshot = categoryCollection.get().await()
        return snapshot.documents.mapNotNull { document ->
            document.toObject(Category::class.java)
                ?.let { document.id to it }
        }.toMap()
    }

    override fun close() {
        listenerRegistration.remove()
    }
}

fun Category.toFirestore(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "timestamp" to timestamp,
        "name" to name,
        "enabled" to enabled
    )
}

fun Map<String, Any?>.toCategory(): Category {

    return Category(
        id = get("id") as? String ?: error("id is null"),
        name = get("name") as? String ?: error("name is null"),
        timestamp = get("timestamp") as? Long ?: error("timestamp is null"),
        enabled = get("enabled") as? Boolean ?: error("enabled is null")
    )
}