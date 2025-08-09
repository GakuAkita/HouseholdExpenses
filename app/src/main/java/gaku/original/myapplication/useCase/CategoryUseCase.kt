package gaku.original.myapplication.useCase

import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.repository.FirestoreRepository.CategoryFirestoreRepository
import gaku.original.myapplication.repository.RealtimeDBrepository.CategoryAssignmentRepository
import gaku.original.myapplication.repository.RealtimeDBrepository.MailboxExtractionRTDbRepository
import javax.inject.Inject

class CategoryUseCase @Inject constructor(
    private val categoryFirestoreRepository: CategoryFirestoreRepository,
    private val repeatAddUseCase: RepeatAddUseCase,
    private val mailboxExtractionRTDbRepository: MailboxExtractionRTDbRepository,
    private val categoryAssignmentUseCase: CategoryAssignmentRepository
) {

    /**
     * categoryIdがRepeatAddやカテゴリー割当にないかチェックする
     */
    suspend fun removeCategory(category: Category) {
    }
}