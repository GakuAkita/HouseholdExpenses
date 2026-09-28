package gaku.original.myapplication.data.datasource.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import gaku.original.myapplication.data.datasource.room.entity.CategoryEntity

/**
 * Reads and writes the local backup of categories.
 */
@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE userId = :userId")
    suspend fun getCategories(userId: String): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE userId = :userId")
    suspend fun deleteCategories(userId: String)

    /** Replaces the backup of [userId] with [categories] in one transaction. */
    @Transaction
    suspend fun replaceCategories(userId: String, categories: List<CategoryEntity>) {
        deleteCategories(userId)
        insertCategories(categories)
    }
}
