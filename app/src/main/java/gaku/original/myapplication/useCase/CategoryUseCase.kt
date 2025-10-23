package gaku.original.myapplication.useCase

import android.util.Log
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.Interface.HasCategoryId
import gaku.original.myapplication.data.FuncStatusInfo
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.RepeatAdd
import gaku.original.myapplication.data.dataClass.getAllAssignments
import gaku.original.myapplication.data.dataClass.getAllEmailTemplateTypes
import gaku.original.myapplication.repository.FirestoreRepository.CategoryFirestoreRepository
import gaku.original.myapplication.repository.FirestoreRepository.RepeatAddFirestoreRepository
import gaku.original.myapplication.repository.LocalRepository.CategoryLocalRepository
import gaku.original.myapplication.repository.RealtimeDBrepository.CategoryAssignmentRepository
import gaku.original.myapplication.repository.RealtimeDBrepository.MailboxExtractionRTDbRepository
import gaku.original.myapplication.utility.LogAkitaDebug
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryFirestoreRepository,
    private val categoryLocalRepository: CategoryLocalRepository,
    private val repeatAddRepository: RepeatAddFirestoreRepository,
    private val mailboxExtractionRepository: MailboxExtractionRTDbRepository,
    private val categoryAssignmentRepository: CategoryAssignmentRepository
) {
    private val className = this::class.simpleName ?: "UnableToGetClassName"

    /**
     * カテゴリを取得（ローカルキャッシュファースト戦略）
     * 1. ローカルRoomDBから読み込み（即座に返す）
     * 2. バックグラウンドでFirebaseから取得してローカルDBを完全同期（トランザクション）
     */
    suspend fun fetchAllCategories(): FuncResultWithData<List<Category>> {
        // まずローカルDBから読み込み
        val localResult = categoryLocalRepository.getAllCategories()
        if (localResult is FuncResultWithData.Success && localResult.data.isNotEmpty()) {
            Log.d(className, "Using cached categories from Room DB (${localResult.data.size} items)")
        }

        // Firebaseから最新データを取得
        val firebaseResult = categoryRepository.fetchAllCategories()
        
        // Firebase取得成功時はローカルDBを完全置き換え（トランザクション）
        if (firebaseResult is FuncResultWithData.Success) {
            categoryLocalRepository.replaceAllCategories(firebaseResult.data)
            Log.d(className, "Synced local category cache with Firebase (${firebaseResult.data.size} items)")
            return firebaseResult
        }
        
        // Firebase取得失敗時はローカルDBデータを返す（存在する場合）
        if (localResult is FuncResultWithData.Success && localResult.data.isNotEmpty()) {
            Log.d(className, "Firebase fetch failed, using cached categories as fallback")
            return localResult
        }
        
        // ローカルもFirebaseも失敗
        return firebaseResult
    }

    /**
     * ローカルDBのみから取得（即座に返す）
     */
    suspend fun getCachedCategories(): FuncResultWithData<List<Category>> {
        return categoryLocalRepository.getAllCategories()
    }

    /**
     * ローカルDBのカテゴリをFlowで監視
     */
    fun getCategoriesFlow(): Flow<List<Category>> {
        return categoryLocalRepository.getAllCategoriesFlow()
    }

    /**
     * ローカルキャッシュをクリア
     */
    suspend fun clearLocalCache() {
        categoryLocalRepository.deleteAllCategories()
    }

    /* SharedViewModel側で既存ダブりチェックをする */
    suspend fun addCategory(category: Category): FuncResultWithData<Category> {
        val result = categoryRepository.addCategory(category)
        
        // Firebaseへの追加成功時、ローカルDBにも保存
        if (result is FuncResultWithData.Success) {
            categoryLocalRepository.insertCategory(result.data)
        }
        
        return result
    }

    suspend fun updateCategory(category: Category): FuncStatusInfo {
        val repeatAddRet = repeatAddRepository.fetchAllRepeatAdd()
        if (repeatAddRet !is FuncResultWithData.Success) {
            return repeatAddRet.toFuncStatusInfo()
        }
        val repeatAddList: List<RepeatAdd> = repeatAddRet.data

        val categoryUpdateStatus = categoryRepository.updateCategory(
            category = category
        )

        if (categoryUpdateStatus.status != FuncStatus.SUCCESS) {
            return categoryUpdateStatus
        }

        // Firebaseへの更新成功時、ローカルDBも更新
        categoryLocalRepository.updateCategory(category)

        for (repeatAdd in repeatAddList) {
            if (repeatAdd.expense.category?.id == category.id) {
                repeatAdd.expense.category = category
                val status = repeatAddRepository.updateRepeatAdd(repeatAdd)
                /* ここの失敗がUI側に伝わらない */
                break
            }
        }
        return categoryUpdateStatus
    }

    /**
     * categoryIdがRepeatAddやカテゴリー割当にないかチェックする
     */
    suspend fun removeCategory(category: Category): FuncStatusInfo {
        if (category.id == null) {
            /* ここに来ることは基本ない */
            val statusInfo = FuncStatusInfo(
                FuncStatus.FAILED,
                "カテゴリーIDがnullのため、削除できません"
            )
            return statusInfo
        }

        val repeatAddRet = checkRepeatAddExists(category.id ?: "")
        if (repeatAddRet.status != FuncStatus.SUCCESS) {
            LogAkitaDebug(repeatAddRet.errorMessage)
            return repeatAddRet
        }

        val assignmentExistCheck = checkCategoryExistInCategoryAssignment(category.id ?: "")
        if (assignmentExistCheck.status != FuncStatus.SUCCESS) {
            LogAkitaDebug(assignmentExistCheck.errorMessage)
            return assignmentExistCheck
        }

        val emailTemplateTypeExistCheck = checkCategoryExistInEmailTemplateType(category.id ?: "")
        if (emailTemplateTypeExistCheck.status != FuncStatus.SUCCESS) {
            LogAkitaDebug(emailTemplateTypeExistCheck.errorMessage)
            return emailTemplateTypeExistCheck
        }

        val removeStatus = categoryRepository.removeCategory(category)
        
        // Firebaseから削除成功時、ローカルDBからも削除
        if (removeStatus.status == FuncStatus.SUCCESS) {
            categoryLocalRepository.deleteCategory(category)
        }
        
        return removeStatus
    }

    suspend fun checkRepeatAddExists(
        categoryId: String
    ): FuncStatusInfo {
        val resultStatus = repeatAddRepository.fetchAllRepeatAdd()
        if (resultStatus !is FuncResultWithData.Success) {
            return resultStatus.toFuncStatusInfo()
        }
        val repeatAddList: List<RepeatAdd> = resultStatus.data

        /* すでに使われているかチェック */
        val exists = repeatAddList.any { it.expense.category?.id == categoryId }
        if (exists) {
            val statusInfo = FuncStatusInfo(
                FuncStatus.FAILED,
                "このカテゴリーは繰り返し追加に登録されています。\n" +
                        "繰り返し追加を削除or編集してからカテゴリーを削除してください。"
            )
            return statusInfo
        }

        val statusInfo = FuncStatusInfo(
            FuncStatus.SUCCESS,
            ""
        )
        return statusInfo
    }

    suspend fun checkCategoryExistInCategoryAssignment(
        categoryId: String
    ): FuncStatusInfo {
        val assignmentDataRet = categoryAssignmentRepository.getCategoryAssignmentData()
        if (assignmentDataRet !is FuncResultWithData.Success) {
            return assignmentDataRet.toFuncStatusInfo()
        }

        val categoryAssignmentData = assignmentDataRet.data
        val allAssignments = categoryAssignmentData.getAllAssignments()
        if (allAssignments.isEmpty()) {
            return FuncStatusInfo(
                FuncStatus.SUCCESS,
                "カテゴリー割当が何もないのでダブりチェックの必要ありません"
            )
        }

        for (assignment in allAssignments) {
            if (assignment.categoryId == categoryId) {
                return FuncStatusInfo(
                    FuncStatus.FAILED,
                    "カテゴリー割当に存在しているので削除できません。そちらを変更してからカテゴリー削除をしてください。名前：${assignment.name}"
                )
            }
        }

        return FuncStatusInfo(
            FuncStatus.SUCCESS,
            "カテゴリー割当には重複がありませんでした"
        )
    }

    suspend fun checkCategoryExistInEmailTemplateType(
        categoryId: String
    ): FuncStatusInfo {
        val allTemplates = getAllEmailTemplateTypes()
        for (template in allTemplates) {
            val fetchResult = mailboxExtractionRepository.getMailTypeSetting(template)
            if (fetchResult !is FuncResultWithData.Success) {
                return FuncStatusInfo(
                    status = FuncStatus.FAILED,
                    errorMessage = "メールボックス設定(${template.menuName})とのダブりチェックでエラーが発生しました。${fetchResult.toFuncStatusInfo().errorMessage}"
                )
            }
            val setting = fetchResult.data
            if (setting is HasCategoryId) {
                /* categoryIdを持っているので、被っていないかチェック */
                if (categoryId == setting.categoryId) {
                    return FuncStatusInfo(
                        status = FuncStatus.FAILED,
                        errorMessage = "メールボックス設定(${template.menuName})に存在しているので削除できません。そちらを変更してからカテゴリー削除をしてください。"
                    )
                }
            }
        }

        return FuncStatusInfo(
            status = FuncStatus.SUCCESS,
            errorMessage = "メールボックス設定には重複がありませんでした"
        )
    }
}