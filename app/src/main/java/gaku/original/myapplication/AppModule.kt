package gaku.original.myapplication

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import gaku.original.myapplication.data.FirestoreRepository.CategoryFirestoreRepository
import gaku.original.myapplication.data.FirestoreRepository.ExpenseFirestoreRepository
import gaku.original.myapplication.data.RealtimeDBrepository.CategoryRepository
import gaku.original.myapplication.data.RealtimeDBrepository.ExpenseRepository
import gaku.original.myapplication.data.RealtimeDBrepository.RepeatAddRepository
import gaku.original.myapplication.viewModel.ExpenseSharedViewModel
import gaku.original.myapplication.viewModel.TemporaryExpenseViewModel

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
    fun provideDbListenerManager(realtimeDbReference: RealtimeDbReference): DbListenerManager {
        return DbListenerManager(realtimeDbReference)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideFirestoreListenerManager(firestoreReference: FirestoreReference): FirestoreListenerManager {
        return FirestoreListenerManager(firestoreReference)
    }

    /************************** Repository類 ******************************/
    @Provides
    @ActivityRetainedScoped
    fun provideExpenseRepository(realtimeDbReference: RealtimeDbReference): ExpenseRepository {
        return ExpenseRepository(realtimeDbReference)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideCategoryRepository(realtimeDbReference: RealtimeDbReference): CategoryRepository {
        return CategoryRepository(realtimeDbReference)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideRepeatAddRepository(realtimeDbReference: RealtimeDbReference): RepeatAddRepository {
        return RepeatAddRepository(realtimeDbReference)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideExpenseFirestoreRepository(firestoreReference: FirestoreReference): ExpenseFirestoreRepository {
        return ExpenseFirestoreRepository(firestoreReference)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideCategoryFirestoreRepository(firestoreReference: FirestoreReference): CategoryFirestoreRepository {
        return CategoryFirestoreRepository(firestoreReference)
    }
    /* ------------------------------------------------------------------ */

    @Provides
    @ActivityRetainedScoped//つけなくてもよい？
    fun provideExpenseSharedViewModel(
        expenseRepository: ExpenseFirestoreRepository,
        categoryRepository: CategoryRepository,
        dbListenerManager: DbListenerManager,
    ): ExpenseSharedViewModel {
        return ExpenseSharedViewModel(
            expenseRepository,
            categoryRepository,
            dbListenerManager,
        )
    }

    @Provides
    @ActivityRetainedScoped
    fun provideTemporaryExpenseViewModel(): TemporaryExpenseViewModel {
        return TemporaryExpenseViewModel()
    }
}