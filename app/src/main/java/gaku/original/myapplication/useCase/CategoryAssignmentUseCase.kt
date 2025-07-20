package gaku.original.myapplication.useCase

import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.data.CheckResult
import gaku.original.myapplication.data.Constants.Status.CheckStatus
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.FetchResult
import gaku.original.myapplication.data.Interface.CategoryAssignable
import gaku.original.myapplication.data.Interface.isProductName
import gaku.original.myapplication.data.Interface.isStoreName
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.data.dataClass.CategoryAssignmentData
import gaku.original.myapplication.data.dataClass.checkAssignment
import gaku.original.myapplication.data.dataClass.checkAssignmentInput
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
        type: CategoryAssignable
    ): FetchResult<DatabaseReference> {
        if (type.isStoreName()) {
            return categoryAssignmentRepository.getStoreNameCategoryAssignmentRef()
        } else if (type.isProductName()) {
            return categoryAssignmentRepository.getProductNameCategoryAssignmentRef()
        } else {
            /* Noneの場合もこっちに来る */
            return FetchResult.Failure.GenericFailure(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "category assign flag is not accepted ${type.categoryAssignFlag}"
            )
        }
    }

    suspend fun getCategoryAssignmentData(): FetchResult<CategoryAssignmentData> {
        return categoryAssignmentRepository.getCategoryAssignmentData()
    }

    /**
     * ダブりチェックを含める
     */
    suspend fun addCategoryAssignmentWithCheck(
        categoryAssignment: CategoryAssignment,
        type: CategoryAssignable,
    ): SuspendFuncStatusInfo {
        val refRet = getCategoryAssignmentRef(
            type = type
        )
        if (refRet !is FetchResult.Success) {
            return refRet.toSuspendFuncStatusInfo()
        }
        val reference = refRet.data

        /**
         * ここでダブりチェックをいれておきたいな。
         * まあ追加するのは頻繁に起こる作業ではないから、逐一リモートから取ればいいか。
         */
        val dataRet = categoryAssignmentRepository.getCategoryAssignments(reference)
        if (dataRet !is FetchResult.Success) {
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
        type: CategoryAssignable
    ): SuspendFuncStatusInfo {
        val refRet = getCategoryAssignmentRef(type)
        if (refRet !is FetchResult.Success) {
            return refRet.toSuspendFuncStatusInfo()
        }
        val reference = refRet.data

        val checkRet = checkAssignmentInput(categoryAssignment)
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
        type: CategoryAssignable
    ): SuspendFuncStatusInfo {
        val refRet = getCategoryAssignmentRef(type)
        if (refRet !is FetchResult.Success) {
            return refRet.toSuspendFuncStatusInfo()
        }
        val reference = refRet.data
        return categoryAssignmentRepository.removeCategoryAssignment(
            categoryAssignment = categoryAssignment,
            reference = reference
        )
    }
}