package gaku.original.myapplication.data

import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.RealtimeDbReference
import kotlinx.coroutines.tasks.await

class CategoryRepository(
    private val realtimeDbReference: RealtimeDbReference
) {

    val categoryRef : DatabaseReference
        get()= realtimeDbReference.getUserCategoryRef()

    suspend fun fetchAllCategories(
        callback: (Boolean) -> Unit = {}
    ):List<Category>{
        try {
            val snapshot = categoryRef.get().await()
            val categories = snapshot.children.mapNotNull {
                it.getValue(Category::class.java)
            }
            Log.d("CategoryRepository", "Fetched Categories: $categories")
            callback(true)
            return categories
        } catch (e: Exception) {
            Log.d("CategoryRepository", "fetchUserExpenses failed. ${e.message}")
            callback(false)
            return emptyList()  // エラー時には空のリストを返す
        }
    }

    fun addCategory(category: Category,callback: (Boolean) -> Unit={}){
        val newCategoryRef = categoryRef.push() // Generate the unique key

        // Create a new instance of Expense with the generated ID
        val categoryWithId = category.copy(
            id = newCategoryRef.key,
            timestamp = System.currentTimeMillis()//時間は念のためここで代入
            )

        // Save the new instance with the generated key
        newCategoryRef.setValue(categoryWithId)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("CategoryRepository", "Category added successfully${category.id}")
                } else {
                    Log.e("CategoryRepository", "Failed to add category", task.exception)
                }
            }
    }

    fun updateCategory(category: Category,callback:(Boolean)->Unit={}){
        // Use the expense's ID (which is the Firebase-generated key) to locate it
        val categoryToUpdateRef = categoryRef.child(category.id ?: return)

        categoryToUpdateRef.setValue(category)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("CategoryRepository", "Category updated successfully${category.id}")
                    callback(true)
                } else {
                    Log.e("CategoryRepository", "Failed to update category", task.exception)
                    callback(false)
                }
            }
    }

    fun removeCategory(category: Category,callback: (Boolean) -> Unit={}){
        val categoryToRemoveRef = categoryRef.child(category.id ?: return)
        categoryToRemoveRef.removeValue()
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Log.d("CategoryRepository","Category removed successfully")
                    callback(true)
                } else{
                    Log.e("CategoryRepository","Failed to remove category",task.exception)
                }
            }
    }
}