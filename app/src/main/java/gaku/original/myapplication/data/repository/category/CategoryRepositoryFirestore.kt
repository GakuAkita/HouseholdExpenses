package gaku.original.myapplication.data.repository.category

import com.google.firebase.firestore.FirebaseFirestore
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.domain.AppUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class CategoryRepositoryFirestore(
    appUser: AppUser,
    firestore: FirebaseFirestore
) : CategoryRepository {

    private val categoryCollection =
        firestore.collection("users").document(appUser.id!!).collection("categories")

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
                    document.toObject(Category::class.java)
                        ?.let { document.id to it }
                }
                .toMap()

            _categories.value = categories
        }

    override suspend fun addCategory(category: Category): Category {
        val newId = categoryCollection.document().id
        val newCategory = category.copy(id = newId)
        categoryCollection.document(newId).set(newCategory)

        return newCategory
    }

    override suspend fun updateCategory(category: Category) {
        categoryCollection.document(category.id!!).set(category)
    }

    override suspend fun deleteCategory(categoryId: String) {
        categoryCollection.document(categoryId).delete()
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