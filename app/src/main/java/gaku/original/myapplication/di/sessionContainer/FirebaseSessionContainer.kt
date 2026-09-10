package gaku.original.myapplication.di.sessionContainer

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import gaku.original.myapplication.data.repository.amazonSubscribeItem.AmazonSubscribeItemRepository
import gaku.original.myapplication.data.repository.amazonSubscribeItem.FakeAmazonSubscribeItemRepository
import gaku.original.myapplication.data.repository.appTimeZone.AppTimeZoneRepository
import gaku.original.myapplication.data.repository.appTimeZone.FakeAppTimeZoneRepository
import gaku.original.myapplication.data.repository.category.CategoryRepository
import gaku.original.myapplication.data.repository.category.CategoryRepositoryFirestore
import gaku.original.myapplication.data.repository.categoryAssignment.CategoryAssignmentRepository
import gaku.original.myapplication.data.repository.emailConnect.EmailConnectionRepository
import gaku.original.myapplication.data.repository.emailConnect.FakeEmailConnectionRepository
import gaku.original.myapplication.data.repository.expense.ExpenseRepository
import gaku.original.myapplication.data.repository.expense.ExpenseRepositoryFirestore
import gaku.original.myapplication.data.repository.mailboxExtraction.FakeMailboxExtractionRepository
import gaku.original.myapplication.data.repository.mailboxExtraction.MailboxExtractionRepository
import gaku.original.myapplication.data.repository.repeatAdd.FakeRepeatAddRepository
import gaku.original.myapplication.data.repository.repeatAdd.RepeatAddRepository

class FirebaseSessionContainer(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val firebaseRealtimeDb: FirebaseDatabase
) : SessionContainer {
    override val categoryRepository: CategoryRepository = CategoryRepositoryFirestore()
    override val expenseRepository: ExpenseRepository = ExpenseRepositoryFirestore(
        firestore = firestore
    )
    override val appTimeZoneRepository: AppTimeZoneRepository = FakeAppTimeZoneRepository()
    override val repeatAddRepository: RepeatAddRepository = FakeRepeatAddRepository()
    override val mailboxExtractionRepository: MailboxExtractionRepository =
        FakeMailboxExtractionRepository()
    override val emailConnectionRepository: EmailConnectionRepository =
        FakeEmailConnectionRepository()

    override val amazonSubscribeItemRepository: AmazonSubscribeItemRepository =
        FakeAmazonSubscribeItemRepository()
    override val categoryAssignmentRepository: CategoryAssignmentRepository =
}