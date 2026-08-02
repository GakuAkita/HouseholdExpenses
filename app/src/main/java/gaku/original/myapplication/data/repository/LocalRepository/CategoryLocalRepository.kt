package gaku.original.myapplication.data.repository.LocalRepository

import android.util.Log
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.local.dao.CategoryDao
import gaku.original.myapplication.data.local.entity.toCategory
import gaku.original.myapplication.data.local.entity.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Categoryのローカルデータベース操作を管理するRepository
 * Roomを使用してカテゴリをローカルに保存・取得
 */
class CategoryLocalRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    private val className = this::class.simpleName ?: "UnableToGetClassName"

    /**
     * 全てのカテゴリを取得
     * @return カテゴリのリスト
     */
    suspend fun getAllCategories(): FuncResultWithData<List<Category>> {
        return try {
            val entities = categoryDao.getAllCategories()                   
            val categories = entities.map { it.toCategory() }
            Log.d(className, "Loaded ${categories.size} categories from local DB")
            FuncResultWithData.Success(data = categories)
        } catch (e: Exception) {
            Log.e(className, "Failed to load categories from local DB: ${e.message}")
            FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = "ローカルDBからのカテゴリ取得に失敗: ${e.message}"
            )
        }
    }

    /**
     * 全てのカテゴリをFlowで取得（リアルタイム更新）
     * @return カテゴリのFlow
     */
    fun getAllCategoriesFlow(): Flow<List<Category>> {
        return categoryDao.getAllCategoriesFlow().map { entities ->
            entities.map { it.toCategory() }
        }
    }

    /**
     * IDでカテゴリを取得
     * @param id カテゴリID
     * @return カテゴリの取得結果
     */
    suspend fun getCategoryById(id: String): FuncResultWithData<Category?> {
        return try {
            val entity = categoryDao.getCategoryById(id)
            if (entity != null) {
                val category = entity.toCategory()
                FuncResultWithData.Success(data = category)
            } else {
                FuncResultWithData.Failure.GenericFailure(
                    status = FuncStatus.FAILED,
                    errorMessage = "ID: $id のカテゴリが見つかりませんでした"
                )
            }
        } catch (e: Exception) {
            Log.e(className, "Failed to get category by id: ${e.message}")
            FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = "カテゴリの取得に失敗: ${e.message}"
            )
        }
    }

    /**
     * カテゴリを挿入または更新
     * @param category 挿入するカテゴリ
     */
    suspend fun insertCategory(category: Category): FuncResultWithData<Category> {
        return try {
            val entity = category.toEntity()
            if (entity == null) {
                return FuncResultWithData.Failure.GenericFailure(
                    status = FuncStatus.FAILED,
                    errorMessage = "カテゴリIDがnullのため保存できません"
                )
            }
            categoryDao.insertCategory(entity)
            Log.d(className, "Inserted category: ${category.name}")
            FuncResultWithData.Success(data = category)
        } catch (e: Exception) {
            Log.e(className, "Failed to insert category: ${e.message}")
            FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = "カテゴリの保存に失敗: ${e.message}"
            )
        }
    }

    /**
     * 複数のカテゴリを挿入または更新（一括保存）
     * @param categories 挿入するカテゴリのリスト
     */
    suspend fun insertCategories(categories: List<Category>): FuncResultWithData<List<Category>> {
        return try {
            val entities = categories.mapNotNull { it.toEntity() }
            if (entities.isEmpty()) {
                return FuncResultWithData.Success(data = emptyList())
            }
            categoryDao.insertCategories(entities)
            Log.d(className, "Inserted ${entities.size} categories to local DB")
            FuncResultWithData.Success(data = categories)
        } catch (e: Exception) {
            Log.e(className, "Failed to insert categories: ${e.message}")
            FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = "カテゴリの一括保存に失敗: ${e.message}"
            )
        }
    }

    /**
     * Firebaseから取得したカテゴリでローカルDBを完全置き換え（トランザクション）
     * ローカルにしかないカテゴリも削除される
     * @param categories Firebaseから取得したカテゴリリスト
     */
    suspend fun replaceAllCategories(categories: List<Category>): FuncResultWithData<List<Category>> {
        return try {
            val entities = categories.mapNotNull { it.toEntity() }
            categoryDao.replaceAllCategories(entities)
            Log.d(className, "Replaced all categories in local DB with ${entities.size} items from Firebase")
            FuncResultWithData.Success(data = categories)
        } catch (e: Exception) {
            Log.e(className, "Failed to replace all categories: ${e.message}")
            FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = "カテゴリの置き換えに失敗: ${e.message}"
            )
        }
    }

    /**
     * カテゴリを更新
     * @param category 更新するカテゴリ
     */
    suspend fun updateCategory(category: Category): FuncResultWithData<Category> {
        return try {
            val entity = category.toEntity()
            if (entity == null) {
                return FuncResultWithData.Failure.GenericFailure(
                    status = FuncStatus.FAILED,
                    errorMessage = "カテゴリIDがnullのため更新できません"
                )
            }
            categoryDao.updateCategory(entity)
            Log.d(className, "Updated category: ${category.name}")
            FuncResultWithData.Success(data = category)
        } catch (e: Exception) {
            Log.e(className, "Failed to update category: ${e.message}")
            FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = "カテゴリの更新に失敗: ${e.message}"
            )
        }
    }

    /**
     * カテゴリを削除
     * @param category 削除するカテゴリ
     */
    suspend fun deleteCategory(category: Category): FuncResultWithData<Unit> {
        return try {
            val categoryId = category.id
            if (categoryId == null) {
                return FuncResultWithData.Failure.GenericFailure(
                    status = FuncStatus.FAILED,
                    errorMessage = "カテゴリIDがnullのため削除できません"
                )
            }
            categoryDao.deleteCategoryById(categoryId)
            Log.d(className, "Deleted category: ${category.name}")
            FuncResultWithData.Success(data = Unit)
        } catch (e: Exception) {
            Log.e(className, "Failed to delete category: ${e.message}")
            FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = "カテゴリの削除に失敗: ${e.message}"
            )
        }
    }

    /**
     * 全てのカテゴリを削除
     */
    suspend fun deleteAllCategories(): FuncResultWithData<Unit> {
        return try {
            categoryDao.deleteAllCategories()
            Log.d(className, "Deleted all categories from local DB")
            FuncResultWithData.Success(data = Unit)
        } catch (e: Exception) {
            Log.e(className, "Failed to delete all categories: ${e.message}")
            FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = "全カテゴリの削除に失敗: ${e.message}"
            )
        }
    }

    /**
     * カテゴリ数を取得
     * @return カテゴリの総数
     */
    suspend fun getCategoryCount(): FuncResultWithData<Int> {
        return try {
            val count = categoryDao.getCategoryCount()
            FuncResultWithData.Success(data = count)
        } catch (e: Exception) {
            Log.e(className, "Failed to get category count: ${e.message}")
            FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = "カテゴリ数の取得に失敗: ${e.message}"
            )
        }
    }

    /**
     * ローカルDBにデータが存在するかチェック
     * @return データが存在する場合はtrue
     */
    suspend fun hasLocalData(): Boolean {
        val countResult = getCategoryCount()
        return if (countResult is FuncResultWithData.Success) {
            countResult.data > 0
        } else {
            false
        }
    }
}

