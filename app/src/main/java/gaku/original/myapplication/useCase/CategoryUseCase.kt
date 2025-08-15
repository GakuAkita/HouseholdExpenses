package gaku.original.myapplication.useCase

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
import gaku.original.myapplication.repository.RealtimeDBrepository.CategoryAssignmentRepository
import gaku.original.myapplication.repository.RealtimeDBrepository.MailboxExtractionRTDbRepository
import gaku.original.myapplication.utility.LogAkitaDebug
import javax.inject.Inject

class CategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryFirestoreRepository,
    private val repeatAddRepository: RepeatAddFirestoreRepository,
    private val mailboxExtractionRepository: MailboxExtractionRTDbRepository,
    private val categoryAssignmentRepository: CategoryAssignmentRepository
) {

    suspend fun fetchAllCategories(): FuncResultWithData<List<Category>> {
        return categoryRepository.fetchAllCategories()
    }

    /* SharedViewModel側で既存ダブりチェックをする */
    suspend fun addCategory(category: Category): FuncResultWithData<Category> {
        return categoryRepository.addCategory(category)
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

        return categoryRepository.removeCategory(category)
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