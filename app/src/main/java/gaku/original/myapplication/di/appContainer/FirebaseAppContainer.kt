package gaku.original.myapplication.di.appContainer

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.firestore.FirebaseFirestore
import gaku.original.myapplication.data.repository.auth.AuthRepository
import gaku.original.myapplication.data.repository.auth.FirebaseAuthRepository
import gaku.original.myapplication.di.sessionContainer.FirebaseSessionContainer
import gaku.original.myapplication.di.sessionContainer.SessionContainer
import gaku.original.myapplication.service.ocr.MlkitOcrService
import gaku.original.myapplication.service.ocr.OcrService
import timber.log.Timber

class FirebaseAppContainer(
    context: Context
) : AppContainer(context = context) {
    val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    val firebaseRealtimeDb: FirebaseDatabase = Firebase.database

    init {
        Timber.d("Firebase Container")
    }

    override val ocrService: OcrService = MlkitOcrService()

    override val authRepository: AuthRepository = FirebaseAuthRepository(firebaseAuth)

    override fun createSessionContainer(): SessionContainer {
        return FirebaseSessionContainer(
            appUser = authRepository.user!!,
            firebaseAuth = firebaseAuth,
            firestore = firestore,
            firebaseRealtimeDb = firebaseRealtimeDb,
            ocrService = ocrService,
            context = context
        )
    }
}