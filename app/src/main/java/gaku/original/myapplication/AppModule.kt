package gaku.original.myapplication

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.components.SingletonComponent
import gaku.original.myapplication.data.local.AppDatabase
import gaku.original.myapplication.data.local.dao.CategoryDao
import gaku.original.myapplication.data.repository.auth.FirebaseAuthRepository
import gaku.original.myapplication.data.repository.FirestoreRepository.CategoryFirestoreRepository
import gaku.original.myapplication.data.repository.FirestoreRepository.ExpenseFirestoreRepository
import gaku.original.myapplication.data.repository.FirestoreRepository.RepeatAddFirestoreRepository
import gaku.original.myapplication.data.repository.FirestoreRepository.UserSettingsFirestoreRepository
import gaku.original.myapplication.data.repository.LocalRepository.CategoryLocalRepository
import gaku.original.myapplication.data.repository.RealtimeDBrepository.AmazonSubscribeItemsRTDbRepository
import gaku.original.myapplication.data.repository.RealtimeDBrepository.CategoryAssignmentRepository
import gaku.original.myapplication.data.repository.RealtimeDBrepository.MailboxExtractionRTDbRepository
import gaku.original.myapplication.data.repository.SharedPreferencesRepository
import gaku.original.myapplication.useCase.CategoryAssignmentUseCase
import gaku.original.myapplication.useCase.CategoryUseCase
import gaku.original.myapplication.useCase.RepeatAddUseCase
import gaku.original.myapplication.viewModel.shared.ExpenseSharedViewModel
import gaku.original.myapplication.viewModel.shared.SharedImageViewModel
import gaku.original.myapplication.viewModel.shared.SharedNotificationListenerViewModel
import gaku.original.myapplication.viewModel.shared.TemporaryExpenseViewModel
import javax.inject.Singleton

@Module
@InstallIn(ActivityRetainedComponent::class)
object AppModule {

//    @Provides
//    @ActivityRetainedScoped
//    fun provideFirebaseAuth(): FirebaseAuth {
//        return FirebaseAuth.getInstance()
//    }
//
//    @Provides
//    @ActivityRetainedScoped
//    fun provideRealtimeDbReference(firebaseAuth: FirebaseAuth): RealtimeDbReference {
//        return RealtimeDbReference(firebaseAuth)
//    }
//
//    @Provides
//    @ActivityRetainedScoped
//    fun provideFirestoreReference(firebaseAuth: FirebaseAuth): FirestoreReference {
//        return FirestoreReference(firebaseAuth)
//    }
//
//    @Provides
//    @ActivityRetainedScoped
//    fun provideFirestoreListenerManager(firestoreReference: FirestoreReference): FirestoreListenerManager {
//        return FirestoreListenerManager(firestoreReference)
//    }
//
//    /************************** Repository類 ******************************/
//    @Provides
//    @ActivityRetainedScoped
//    fun provideExpenseFirestoreRepository(
//        firestoreReference: FirestoreReference
//    ): ExpenseFirestoreRepository {
//        return ExpenseFirestoreRepository(firestoreReference)
//    }
//
//    @Provides
//    @ActivityRetainedScoped
//    fun provideCategoryFirestoreRepository(firestoreReference: FirestoreReference): CategoryFirestoreRepository {
//        return CategoryFirestoreRepository(firestoreReference)
//    }
//
//    @Provides
//    @ActivityRetainedScoped
//    fun provideRepeatAddFirestoreRepository(firestoreReference: FirestoreReference): RepeatAddFirestoreRepository {
//        return RepeatAddFirestoreRepository(firestoreReference)
//    }
//
//    @Provides
//    @ActivityRetainedScoped
//    fun provideUserSettingsFirestoreRepository(
//        firebaseAuth: FirebaseAuth,
//        firestoreReference: FirestoreReference
//    ): UserSettingsFirestoreRepository {
//        return UserSettingsFirestoreRepository(firebaseAuth, firestoreReference)
//    }
//
//    @Provides
//    @ActivityRetainedScoped
//    fun provideMailboxExtractionRTDbRepository(
//        realtimeDbReference: RealtimeDbReference
//    ): MailboxExtractionRTDbRepository {
//        return MailboxExtractionRTDbRepository(realtimeDbReference)
//    }
//
//    @Provides
//    @ActivityRetainedScoped
//    fun provideCategoryAssignmentRTDbRepository(
//        realtimeDbReference: RealtimeDbReference
//    ): CategoryAssignmentRepository {
//        return CategoryAssignmentRepository(realtimeDbReference)
//    }
//
//    @Provides
//    @ActivityRetainedScoped
//    fun provideFirebaseAuthRepository(
//        firebaseAuth: FirebaseAuth
//    ): FirebaseAuthRepository {
//        return FirebaseAuthRepository(firebaseAuth)
//    }
//
//    @Provides
//    @ActivityRetainedScoped
//    fun provideAmazonSubscribeItemsRTDbRepository(
//        realtimeDbReference: RealtimeDbReference
//    ): AmazonSubscribeItemsRTDbRepository {
//        return AmazonSubscribeItemsRTDbRepository(realtimeDbReference)
//    }
//
//
//    @Provides
//    @ActivityRetainedScoped
//    fun provideSharedPreferencesRepository(
//        @ApplicationContext context: Context
//    ): SharedPreferencesRepository {
//        return SharedPreferencesRepository(context)
//    }
//
//    /************************** Room Database (Local) ******************************/
//    @Provides
//    @ActivityRetainedScoped
//    fun provideAppDatabase(
//        @ApplicationContext context: Context
//    ): AppDatabase {
//        return Room.databaseBuilder(
//            context,
//            AppDatabase::class.java,
//            AppDatabase.DATABASE_NAME
//        ).build()
//    }
//
//    @Provides
//    @ActivityRetainedScoped
//    fun provideCategoryDao(appDatabase: AppDatabase): CategoryDao {
//        return appDatabase.categoryDao()
//    }
//
//    @Provides
//    @ActivityRetainedScoped
//    fun provideCategoryLocalRepository(
//        categoryDao: CategoryDao
//    ): CategoryLocalRepository {
//        return CategoryLocalRepository(categoryDao)
//    }
//
//    /* --------------------------UseCase関連---------------------------------- */
//    @Provides
//    @ActivityRetainedScoped
//    fun provideCategoryAssignmentUseCase(
//        categoryAssignmentRepository: CategoryAssignmentRepository
//    ): CategoryAssignmentUseCase {
//        return CategoryAssignmentUseCase(categoryAssignmentRepository)
//    }
//
//    @Provides
//    @ActivityRetainedScoped
//    fun provideRepeatAddUseCase(
//        repeatAddRepository: RepeatAddFirestoreRepository,
//        expenseRepository: ExpenseFirestoreRepository,
//    ): RepeatAddUseCase {
//        return RepeatAddUseCase(repeatAddRepository, expenseRepository)
//    }
//
//    @Provides
//    @ActivityRetainedScoped
//    fun provideCategoryUseCase(
//        categoryRepository: CategoryFirestoreRepository,
//        categoryLocalRepository: CategoryLocalRepository,
//        repeatAddRepository: RepeatAddFirestoreRepository,
//        mailboxExtractionRepository: MailboxExtractionRTDbRepository,
//        categoryAssignmentRepository: CategoryAssignmentRepository
//    ): CategoryUseCase {
//        return CategoryUseCase(
//            categoryRepository,
//            categoryLocalRepository,
//            repeatAddRepository,
//            mailboxExtractionRepository,
//            categoryAssignmentRepository
//        )
//    }
//
//    /* ------------------------------------------------------------------ */
//
//    @Provides
//    @ActivityRetainedScoped//つけなくてもよい？
//    fun provideExpenseSharedViewModel(
//        expenseRepository: ExpenseFirestoreRepository,
//        categoryUseCase: CategoryUseCase,
//        userSettingsFirestoreRepository: UserSettingsFirestoreRepository,
//        firestoreListenerManager: FirestoreListenerManager
//    ): ExpenseSharedViewModel {
//        return ExpenseSharedViewModel(
//            expenseRepository,
//            categoryUseCase,
//            userSettingsFirestoreRepository,
//            firestoreListenerManager
//        )
//    }
//
//    @Provides
//    @ActivityRetainedScoped
//    fun provideTemporaryExpenseViewModel(): TemporaryExpenseViewModel {
//        return TemporaryExpenseViewModel()
//    }
//
//    @Provides
//    @ActivityRetainedScoped
//    fun provideSharedImageViewModel(): SharedImageViewModel {
//        return SharedImageViewModel()
//    }
//
//    @Provides
//    @ActivityRetainedScoped
//    fun provideSharedNotificationListenerViewModel(): SharedNotificationListenerViewModel {
//        return SharedNotificationListenerViewModel()
//    }
}