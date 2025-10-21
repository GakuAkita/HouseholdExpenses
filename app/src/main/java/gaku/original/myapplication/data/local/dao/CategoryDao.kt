package gaku.original.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import gaku.original.myapplication.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * CategoryのDAO (Data Access Object)
 * Roomを使用したローカルデータベース操作
 */
@Dao
interface CategoryDao {
    
    /**
     * 全てのカテゴリを取得
     * @return カテゴリのリスト
     */
    @Query("SELECT * FROM categories ORDER BY timestamp DESC")
    suspend fun getAllCategories(): List<CategoryEntity>

    /**
     * 全てのカテゴリをFlowで取得（リアルタイム更新）
     * @return カテゴリのFlow
     */
    @Query("SELECT * FROM categories ORDER BY timestamp DESC")
    fun getAllCategoriesFlow(): Flow<List<CategoryEntity>>

    /**
     * IDでカテゴリを取得
     * @param id カテゴリID
     * @return カテゴリ、存在しない場合はnull
     */
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: String): CategoryEntity?

    /**
     * カテゴリを挿入（競合時は置き換え）
     * @param category 挿入するカテゴリ
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    /**
     * 複数のカテゴリを挿入（競合時は置き換え）
     * @param categories 挿入するカテゴリのリスト
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    /**
     * カテゴリを更新
     * @param category 更新するカテゴリ
     */
    @Update
    suspend fun updateCategory(category: CategoryEntity)

    /**
     * カテゴリを削除
     * @param category 削除するカテゴリ
     */
    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    /**
     * IDでカテゴリを削除
     * @param id カテゴリID
     */
    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: String)

    /**
     * 全てのカテゴリを削除
     */
    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()

    /**
     * カテゴリ数を取得
     * @return カテゴリの総数
     */
    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    /**
     * 有効なカテゴリのみ取得
     * @return 有効なカテゴリのリスト
     */
    @Query("SELECT * FROM categories WHERE enabled = 1 ORDER BY timestamp DESC")
    suspend fun getEnabledCategories(): List<CategoryEntity>

    /**
     * 全カテゴリを置き換え（トランザクション）
     * 全削除してから新しいデータを挿入
     * @param categories 新しいカテゴリリスト
     */
    @Transaction
    suspend fun replaceAllCategories(categories: List<CategoryEntity>) {
        deleteAllCategories()
        insertCategories(categories)
    }
}

