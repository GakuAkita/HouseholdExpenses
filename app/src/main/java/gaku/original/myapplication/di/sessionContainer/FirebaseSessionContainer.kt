package gaku.original.myapplication.di.sessionContainer

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import gaku.original.myapplication.data.extractor.Extractor
import gaku.original.myapplication.data.extractor.paypayReceipt.PayPayReceiptExtractor
import gaku.original.myapplication.data.extractor.paypayReceipt.PayPayReceiptValidator
import gaku.original.myapplication.data.repository.amazonSubscribeItem.AmazonSubscribeItemRepository
import gaku.original.myapplication.data.repository.amazonSubscribeItem.FakeAmazonSubscribeItemRepository
import gaku.original.myapplication.data.repository.appTimeZone.AppTimeZoneRepository
import gaku.original.myapplication.data.repository.appTimeZone.AppTimeZoneRepositoryFirestore
import gaku.original.myapplication.data.repository.category.CategoryRepository
import gaku.original.myapplication.data.repository.category.CategoryRepositoryFirestore
import gaku.original.myapplication.data.repository.categoryAssignment.CategoryAssignmentRepository
import gaku.original.myapplication.data.repository.categoryAssignment.FakeCategoryAssignmentRepository
import gaku.original.myapplication.data.repository.emailConnect.EmailConnectionRepository
import gaku.original.myapplication.data.repository.emailConnect.FakeEmailConnectionRepository
import gaku.original.myapplication.data.repository.expense.ExpenseRepository
import gaku.original.myapplication.data.repository.expense.ExpenseRepositoryFirestore
import gaku.original.myapplication.data.repository.mailboxExtraction.FakeMailboxExtractionRepository
import gaku.original.myapplication.data.repository.mailboxExtraction.MailboxExtractionRepository
import gaku.original.myapplication.data.repository.paypayReceipt.FakePayPayReceiptConfigRepository
import gaku.original.myapplication.data.repository.paypayReceipt.PayPayReceiptConfigRepository
import gaku.original.myapplication.data.repository.repeatAdd.RepeatAddRepository
import gaku.original.myapplication.data.repository.repeatAdd.RepeatAddRepositoryFirestore
import gaku.original.myapplication.domain.AppUser
import gaku.original.myapplication.service.ocr.OcrService

class FirebaseSessionContainer(
    override val appUser: AppUser,
    private val firestore: FirebaseFirestore,
    private val firebaseRealtimeDb: FirebaseDatabase,
    private val ocrService: OcrService
) : SessionContainer {

    init {
        if (appUser.id == null) {
            throw Exception("Coding Error:AppUser id is null!")
        } else if (appUser.email == null) {
            throw Exception("Coding Error:AppUser email is null!")
        }
    }

    /* order is important */
    private val _paypayReceiptConfigRepository = FakePayPayReceiptConfigRepository()

    private val _paypayReceiptExtractor = PayPayReceiptExtractor(
        _paypayReceiptConfigRepository,
        ocrService
    )

    override val payPayReceiptConfigRepository: PayPayReceiptConfigRepository =
        _paypayReceiptConfigRepository

    override val payPayReceiptExtractor: Extractor = _paypayReceiptExtractor
    override val payPayReceiptValidator: PayPayReceiptValidator = _paypayReceiptExtractor

    override val categoryRepository: CategoryRepository = CategoryRepositoryFirestore(
        appUser = appUser,
        firestore = firestore
    )
    override val expenseRepository: ExpenseRepository = ExpenseRepositoryFirestore(
        appUser = appUser,
        firestore = firestore
    )
    override val appTimeZoneRepository: AppTimeZoneRepository = AppTimeZoneRepositoryFirestore(
        appUser = appUser,
        firestore = firestore
    )
    override val repeatAddRepository: RepeatAddRepository = RepeatAddRepositoryFirestore(
        appUser = appUser,
        firestore = firestore
    )
    override val mailboxExtractionRepository: MailboxExtractionRepository =
        FakeMailboxExtractionRepository()
    override val emailConnectionRepository: EmailConnectionRepository =
        FakeEmailConnectionRepository()

    override val amazonSubscribeItemRepository: AmazonSubscribeItemRepository =
        FakeAmazonSubscribeItemRepository()
    override val categoryAssignmentRepository: CategoryAssignmentRepository =
        FakeCategoryAssignmentRepository()
}