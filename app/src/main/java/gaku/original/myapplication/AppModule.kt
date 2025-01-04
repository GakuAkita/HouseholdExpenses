package gaku.original.myapplication

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.components.SingletonComponent
import gaku.original.myapplication.data.CategoryRepository
import gaku.original.myapplication.data.ExpenseRepository
import gaku.original.myapplication.viewModel.ExpenseSharedViewModel
import gaku.original.myapplication.viewModel.TemporaryExpenseViewModel
import javax.inject.Singleton

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
    fun provideDbListenerManager(realtimeDbReference: RealtimeDbReference): DbListenerManager {
        return DbListenerManager(realtimeDbReference)
    }

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
    @ActivityRetainedScoped//つけなくてもよい？
    fun provideExpenseSharedViewModel(
        expenseRepository: ExpenseRepository,
        categoryRepository: CategoryRepository,
        dbListenerManager: DbListenerManager
    ): ExpenseSharedViewModel {
        return ExpenseSharedViewModel(expenseRepository, categoryRepository,dbListenerManager)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideTemporaryExpenseViewModel(): TemporaryExpenseViewModel {
        return TemporaryExpenseViewModel()
    }
}