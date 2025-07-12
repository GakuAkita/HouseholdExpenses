package gaku.original.myapplication

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import gaku.original.myapplication.data.Repository.FirebaseAuthRepository
import gaku.original.myapplication.data.Repository.FirestoreRepository.CategoryFirestoreRepository
import gaku.original.myapplication.data.Repository.FirestoreRepository.ExpenseFirestoreRepository
import gaku.original.myapplication.data.Repository.FirestoreRepository.RepeatAddFirestoreRepository
import gaku.original.myapplication.data.Repository.FirestoreRepository.UserSettingsFirestoreRepository
import gaku.original.myapplication.data.Repository.RealtimeDBrepository.MailboxExtractionRTDbRepository
import gaku.original.myapplication.viewModel.ExpenseSharedViewModel
import gaku.original.myapplication.viewModel.main.TemporaryExpenseViewModel

@Module
@InstallIn(ActivityRetainedComponent::class)
object AppModule {

    @Provides
    @ActivityRetainedScoped
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @ActivityRetainedScoped
    fun provideRealtimeDbReference(firebaseAuth: FirebaseAuth): RealtimeDbReference {
        return RealtimeDbReference(firebaseAuth)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideFirestoreReference(firebaseAuth: FirebaseAuth): FirestoreReference {
        return FirestoreReference(firebaseAuth)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideFirestoreListenerManager(firestoreReference: FirestoreReference): FirestoreListenerManager {
        return FirestoreListenerManager(firestoreReference)
    }

    /************************** Repository類 ******************************/
    @Provides
    @ActivityRetainedScoped
    fun provideExpenseFirestoreRepository(
        firestoreReference: FirestoreReference
    ): ExpenseFirestoreRepository {
        return ExpenseFirestoreRepository(firestoreReference)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideCategoryFirestoreRepository(firestoreReference: FirestoreReference): CategoryFirestoreRepository {
        return CategoryFirestoreRepository(firestoreReference)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideRepeatAddFirestoreRepository(firestoreReference: FirestoreReference): RepeatAddFirestoreRepository {
        return RepeatAddFirestoreRepository(firestoreReference)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideUserSettingsFirestoreRepository(
        firebaseAuth: FirebaseAuth,
        firestoreReference: FirestoreReference
    ): UserSettingsFirestoreRepository {
        return UserSettingsFirestoreRepository(firebaseAuth, firestoreReference)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideMailboxExtractionRTDbRepository(
        realtimeDbReference: RealtimeDbReference
    ): MailboxExtractionRTDbRepository {
        return MailboxExtractionRTDbRepository(realtimeDbReference)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideFirebaseAuthRepository(
        firebaseAuth: FirebaseAuth
    ): FirebaseAuthRepository {
        return FirebaseAuthRepository(firebaseAuth)
    }

    /* ------------------------------------------------------------------ */

    @Provides
    @ActivityRetainedScoped//つけなくてもよい？
    fun provideExpenseSharedViewModel(
        expenseRepository: ExpenseFirestoreRepository,
        categoryRepository: CategoryFirestoreRepository,
        repeatAddRepository: RepeatAddFirestoreRepository,
        userSettingsFirestoreRepository: UserSettingsFirestoreRepository,
        firestoreListenerManager: FirestoreListenerManager
    ): ExpenseSharedViewModel {
        return ExpenseSharedViewModel(
            expenseRepository,
            categoryRepository,
            repeatAddRepository,
            userSettingsFirestoreRepository,
            firestoreListenerManager
        )
    }

    @Provides
    @ActivityRetainedScoped
    fun provideTemporaryExpenseViewModel(): TemporaryExpenseViewModel {
        return TemporaryExpenseViewModel()
    }
}