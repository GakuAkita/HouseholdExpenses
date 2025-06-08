package gaku.original.myapplication.viewModel.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.FirestoreListenerManager
import gaku.original.myapplication.data.FirestoreRepository.CategoryFirestoreRepository
import gaku.original.myapplication.data.FirestoreRepository.ExpenseFirestoreRepository
import javax.inject.Inject

@HiltViewModel
class NotCategorizedViewModel @Inject constructor(
    private val expenseFirestoreRepository: ExpenseFirestoreRepository,
    private val categoryFirestoreRepository: CategoryFirestoreRepository,
    private val firestoreListenerManager: FirestoreListenerManager
) : ViewModel() {


}