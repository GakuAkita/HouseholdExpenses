package gaku.original.myapplication.useCase

import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.FetchResult
import gaku.original.myapplication.data.Interface.CategoryAssignable
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.CategoryAssignFlag
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.data.dataClass.CategoryAssignmentData
import gaku.original.myapplication.repository.RealtimeDBrepository.CategoryAssignmentRepository
import javax.inject.Inject

class CategoryAssignmentUseCase @Inject constructor(
    private val categoryAssignmentRepository: CategoryAssignmentRepository,
) {
    /**
     * MailboxExtractionTypeに応じてStoreNameなのか、ProductNameなのか切り替えたい
     * STORE_NAMEとPRODUCT_NAMEの両方を持っていたらどうなるだろう？
     * mailboxExtraction以外にも増えてきたら足していくか
     */
    suspend fun getCategoryAssignmentRef(
        type: CategoryAssignable
    ): FetchResult<DatabaseReference> {
        if (type.categoryAssignFlag == CategoryAssignFlag.STORE_NAME) {
            return categoryAssignmentRepository.getStoreNameCategoryAssignmentRef()
        } else if (type.categoryAssignFlag == CategoryAssignFlag.PRODUCT_NAME) {
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

    fun checkDuplicateCategoryAssignment(
        categoryAssignment: CategoryAssignment,
        allAssignments: Map<String, CategoryAssignment>
    ): Boolean {
        for ((_, value) in allAssignments) {
            if ((categoryAssignment.name == value.name) and (categoryAssignment.condition == value.condition)) {
                return true
            }
        }
        return false
    }

    /**
     * ダブりチェックを含める
     */
    suspend fun addCategoryAssignmentWithDuplicateCheck(
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

        val duplicate = checkDuplicateCategoryAssignment(categoryAssignment, data)

        if (duplicate) {
            return SuspendFuncStatusInfo(
                status = SuspendFuncStatus.FAILED,
                errorMessage = "カテゴリー割当が重複しています"
            )
        }
        return categoryAssignmentRepository.addCategoryAssignment(
            categoryAssignment = categoryAssignment,
            reference = reference
        )
    }

    suspend fun updateCategoryAssignmentWithDuplicateCheck(
        categoryAssignment: CategoryAssignment,
        type: CategoryAssignable
    ): SuspendFuncStatusInfo {
        val refRet = getCategoryAssignmentRef(type)
        if (refRet !is FetchResult.Success) {
            return refRet.toSuspendFuncStatusInfo()
        }
        val reference = refRet.data

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