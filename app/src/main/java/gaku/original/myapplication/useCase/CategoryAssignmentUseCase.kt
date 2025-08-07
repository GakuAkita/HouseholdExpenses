package gaku.original.myapplication.useCase

import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.data.CheckResult
import gaku.original.myapplication.data.Constants.Status.CheckStatus
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.Interface.CategoryAssignNamePattern
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.data.dataClass.CategoryAssignmentData
import gaku.original.myapplication.data.dataClass.checkAssignment
import gaku.original.myapplication.repository.RealtimeDBrepository.CategoryAssignmentRepository
import javax.inject.Inject

class CategoryAssignmentUseCase @Inject constructor(
    private val categoryAssignmentRepository: CategoryAssignmentRepository,
) {
    /**
     * EmailTemplateTypeに応じてStoreNameなのか、ProductNameなのか切り替えたい
     * STORE_NAMEとPRODUCT_NAMEの両方を持っていたらどうなるだろう？
     * mailboxExtraction以外にも増えてきたら足していくか
     */
    suspend fun getCategoryAssignmentRef(
        pattern: CategoryAssignNamePattern
    ): FuncResultWithData<DatabaseReference> {
        if (pattern == CategoryAssignNamePattern.STORE) {
            return categoryAssignmentRepository.getStoreNameCategoryAssignmentRef()
        } else if (pattern == CategoryAssignNamePattern.PRODUCT) {
            return categoryAssignmentRepository.getProductNameCategoryAssignmentRef()
        } else {
            /* Noneの場合もこっちに来る */
            return FuncResultWithData.Failure.GenericFailure(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "category assign name pattern is not accepted ${pattern.label}"
            )
        }
    }

    suspend fun getCategoryAssignmentData(): FuncResultWithData<CategoryAssignmentData> {
        return categoryAssignmentRepository.getCategoryAssignmentData()
    }

    /**
     * ダブりチェックを含める
     */
    suspend fun addCategoryAssignmentWithCheck(
        categoryAssignment: CategoryAssignment,
        namePattern: CategoryAssignNamePattern,
    ): SuspendFuncStatusInfo {
        val refRet = getCategoryAssignmentRef(
            pattern = namePattern
        )
        if (refRet !is FuncResultWithData.Success) {
            return refRet.toSuspendFuncStatusInfo()
        }
        val reference = refRet.data

        /**
         * ここでダブりチェックをいれておきたいな。
         * まあ追加するのは頻繁に起こる作業ではないから、逐一リモートから取ればいいか。
         */
        val dataRet = categoryAssignmentRepository.getCategoryAssignments(reference)
        if (dataRet !is FuncResultWithData.Success) {
            val statusInfo = dataRet.toSuspendFuncStatusInfo()
            return SuspendFuncStatusInfo(
                status = statusInfo.status,
                errorMessage = "ダブりチェックのための既存カテゴリー割当の取得に失敗しました:${statusInfo.errorMessage}"
            )
        }
        val data = dataRet.data

        val checkRet: CheckResult = checkAssignment(categoryAssignment, data)

        if (checkRet.status != CheckStatus.OK) {
            return SuspendFuncStatusInfo(
                status = SuspendFuncStatus.FAILED,
                errorMessage = checkRet.errorMessage
            )
        }
        return categoryAssignmentRepository.addCategoryAssignment(
            categoryAssignment = categoryAssignment,
            reference = reference
        )
    }

    suspend fun updateCategoryAssignmentWithCheck(
        categoryAssignment: CategoryAssignment,
        pattern: CategoryAssignNamePattern
    ): SuspendFuncStatusInfo {
        val refRet = getCategoryAssignmentRef(pattern)
        if (refRet !is FuncResultWithData.Success) {
            return refRet.toSuspendFuncStatusInfo()
        }
        val reference = refRet.data

        val dataRet = categoryAssignmentRepository.getCategoryAssignments(reference)
        if (dataRet !is FuncResultWithData.Success) {
            val statusInfo = dataRet.toSuspendFuncStatusInfo()
            return SuspendFuncStatusInfo(
                status = statusInfo.status,
                errorMessage = "ダブりチェックのための既存カテゴリー割当の取得に失敗しました:${statusInfo.errorMessage}"
            )
        }
        val data = dataRet.data

        /**
         * ダブりチェックも含む
         */
        val checkRet = checkAssignment(categoryAssignment, data)
        if (checkRet.status != CheckStatus.OK) {
            return SuspendFuncStatusInfo(
                status = SuspendFuncStatus.FAILED,
                errorMessage = checkRet.errorMessage
            )
        }

        return categoryAssignmentRepository.updateCategoryAssignment(
            categoryAssignment = categoryAssignment,
            reference = reference
        )
    }

    suspend fun removeCategoryAssignment(
        categoryAssignment: CategoryAssignment,
        pattern: CategoryAssignNamePattern
    ): SuspendFuncStatusInfo {
        val refRet = getCategoryAssignmentRef(pattern)
        if (refRet !is FuncResultWithData.Success) {
            return refRet.toSuspendFuncStatusInfo()
        }
        val reference = refRet.data
        return categoryAssignmentRepository.removeCategoryAssignment(
            categoryAssignment = categoryAssignment,
            reference = reference
        )
    }
}