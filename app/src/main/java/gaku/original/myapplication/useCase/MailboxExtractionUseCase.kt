package gaku.original.myapplication.useCase

import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.FetchResult
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.CategoryAssignFlag
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.data.dataClass.CategoryAssignmentData
import gaku.original.myapplication.data.dataClass.MailboxExtractionType
import gaku.original.myapplication.repository.RealtimeDBrepository.CategoryAssignmentRepository
import gaku.original.myapplication.repository.RealtimeDBrepository.MailboxExtractionRTDbRepository
import javax.inject.Inject

class MailboxExtractionUseCase @Inject constructor(
    private val mailboxExtractionRepository: MailboxExtractionRTDbRepository,
    private val categoryAssignmentRepository: CategoryAssignmentRepository
) {
    /**
     * MailboxExtractionTypeに応じてStoreNameなのか、ProductNameなのか切り替えたい
     * STORE_NAMEとPRODUCT_NAMEの両方を持っていたらどうなるだろう？
     */
    suspend fun getCategoryAssignmentRef(
        type: MailboxExtractionType
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


    suspend fun addCategoryAssignment(
        categoryAssignment: CategoryAssignment,
        mailboxExtractionType: MailboxExtractionType,
    ): SuspendFuncStatusInfo {
        val refRet = getCategoryAssignmentRef(
            type = mailboxExtractionType
        )
        if (refRet !is FetchResult.Success) {
            return refRet.toSuspendFuncStatusInfo()
        }
        val reference = refRet.data

        /**
         * ここでダブりチェックをいれておきたいな。
         */

        return categoryAssignmentRepository.addCategoryAssignment(
            categoryAssignment = categoryAssignment,
            reference = reference
        )
    }
}