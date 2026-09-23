package gaku.original.myapplication.di.sessionContainer

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import gaku.original.myapplication.common.CodingErrorException
import gaku.original.myapplication.data.datasource.SharedPreferencesDataSource
import gaku.original.myapplication.data.extractor.Extractor
import gaku.original.myapplication.data.extractor.paypayReceipt.PayPayReceiptExtractor
import gaku.original.myapplication.data.extractor.paypayReceipt.PayPayReceiptValidator
import gaku.original.myapplication.data.firebaseReference.FirestoreUserReference
import gaku.original.myapplication.data.firebaseReference.RealtimeDbUserReference
import gaku.original.myapplication.data.repository.amazonSubscribeItem.AmazonSubscribeItemRepository
import gaku.original.myapplication.data.repository.amazonSubscribeItem.AmazonSubscribeItemRepositoryRealtimeDb
import gaku.original.myapplication.data.repository.appTimeZone.AppTimeZoneRepository
import gaku.original.myapplication.data.repository.appTimeZone.AppTimeZoneRepositoryFirestore
import gaku.original.myapplication.data.repository.category.CategoryRepository
import gaku.original.myapplication.data.repository.category.CategoryRepositoryFirestore
import gaku.original.myapplication.data.repository.categoryAssignment.CategoryAssignmentRepository
import gaku.original.myapplication.data.repository.categoryAssignment.CategoryAssignmentRepositoryRealtimeDb
import gaku.original.myapplication.data.repository.emailConnect.EmailConnectionRepository
import gaku.original.myapplication.data.repository.emailConnect.EmailConnectionRepositoryFirebase
import gaku.original.myapplication.data.repository.expense.ExpenseRepository
import gaku.original.myapplication.data.repository.expense.ExpenseRepositoryFirestore
import gaku.original.myapplication.data.repository.mailboxExtraction.MailboxExtractionRepository
import gaku.original.myapplication.data.repository.mailboxExtraction.MailboxExtractionRepositoryRealtimeDb
import gaku.original.myapplication.data.repository.paypayReceipt.PayPayReceiptConfigRepository
import gaku.original.myapplication.data.repository.paypayReceipt.PayPayReceiptRepositorySharedPreferences
import gaku.original.myapplication.data.repository.repeatAdd.RepeatAddRepository
import gaku.original.myapplication.data.repository.repeatAdd.RepeatAddRepositoryFirestore
import gaku.original.myapplication.domain.AppUser
import gaku.original.myapplication.service.ocr.OcrService

class FirebaseSessionContainer(
    override val appUser: AppUser,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val firebaseRealtimeDb: FirebaseDatabase,
    private val ocrService: OcrService,
    private val context: Context
) : SessionContainer {

    private val sharedPreferencesDataSource = SharedPreferencesDataSource(
        userId = appUser.id!!,
        context = context
    )

    private val firestoreReference = FirestoreUserReference(
        firestore = firestore,
        appUser = appUser
    )

    private val realtimeDbReference = RealtimeDbUserReference(
        appUser = appUser,
        realtimeDb = firebaseRealtimeDb
    )

    init {
        if (appUser.id == null) {
            throw CodingErrorException("AppUser id is null!")
        } else if (appUser.email == null) {
            throw CodingErrorException("AppUser email is null!")
        }
    }


    /* order is important */
    private val _paypayReceiptConfigRepository = PayPayReceiptRepositorySharedPreferences(
        sharedPreferencesDataSource
    )

    private val _paypayReceiptExtractor = PayPayReceiptExtractor(
        _paypayReceiptConfigRepository,
        ocrService
    )

    override val payPayReceiptConfigRepository: PayPayReceiptConfigRepository =
        _paypayReceiptConfigRepository

    override val payPayReceiptExtractor: Extractor = _paypayReceiptExtractor
    override val payPayReceiptValidator: PayPayReceiptValidator = _paypayReceiptExtractor

    override val categoryRepository: CategoryRepository = CategoryRepositoryFirestore(
        firestoreReference
    )
    override val expenseRepository: ExpenseRepository = ExpenseRepositoryFirestore(
        firestoreReference
    )
    override val appTimeZoneRepository: AppTimeZoneRepository = AppTimeZoneRepositoryFirestore(
        firestoreReference
    )
    override val repeatAddRepository: RepeatAddRepository = RepeatAddRepositoryFirestore(
        firestoreReference
    )
    override val mailboxExtractionRepository: MailboxExtractionRepository =
        MailboxExtractionRepositoryRealtimeDb(
            realtimeDbReference = realtimeDbReference
        )
    override val emailConnectionRepository: EmailConnectionRepository =
        EmailConnectionRepositoryFirebase(
            appUser = appUser,
            firebaseAuth = firebaseAuth,
            realtimeDbReference = realtimeDbReference
        )

    override val amazonSubscribeItemRepository: AmazonSubscribeItemRepository =
        AmazonSubscribeItemRepositoryRealtimeDb(
            realtimeDbReference = realtimeDbReference
        )
    override val categoryAssignmentRepository: CategoryAssignmentRepository =
        CategoryAssignmentRepositoryRealtimeDb(
            realtimeDbReference = realtimeDbReference
        )
}