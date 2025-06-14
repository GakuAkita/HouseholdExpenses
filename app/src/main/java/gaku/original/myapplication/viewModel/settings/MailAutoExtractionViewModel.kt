package gaku.original.myapplication.viewModel.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.FirestoreRepository.CategoryFirestoreRepository
import gaku.original.myapplication.data.FirestoreRepository.MailAutoExtractionFirestoreRepository
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.MailAutoExtractionCommon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MailAutoExtractionViewModel @Inject constructor(
    private val categoryFirestoreRepository: CategoryFirestoreRepository,
    private val mailAutoExtractionFirestoreRepository: MailAutoExtractionFirestoreRepository
) : ViewModel() {
    val className: String = this::class.simpleName ?: "UnableToGetClassName"

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> get() = _categories

    /* MailAutoExtractionViewにしか紐づけられないのでbackStackで戻れば毎回Clearされる */
    override fun onCleared() {
        super.onCleared()
        Log.d(className, "$className was Cleared!!")
    }

    init {
        Log.d(className, "$className was Initialized!!")
    }

    fun fetchMailAutoExtractionInternalSetting(
        instance: MailAutoExtractionCommon,
        callback: (SuspendFuncStatusInfo) -> Unit
    ) {
        viewModelScope.launch {
            mailAutoExtractionFirestoreRepository.fetchMailAutoExtractionSetting(
                instance,
                callback = callback
            )
        }
    }

    fun setMailAutoExtractionInternalSetting(
        instance: MailAutoExtractionCommon,
        callback: (SuspendFuncStatusInfo) -> Unit
    ) {
        viewModelScope.launch {
            mailAutoExtractionFirestoreRepository.setMailAutoExtractionInternalSetting(
                instance,
                callback = callback
            )
        }
    }

    fun fetchCategories(callback: (SuspendFuncStatusInfo) -> Unit) {
        viewModelScope.launch {
            val fetchResult = categoryFirestoreRepository.fetchAllCategories(callback = {})
            if (fetchResult.status == SuspendFuncStatus.SUCCESS) {
                _categories.value = fetchResult.data ?: emptyList()
            }
            callback(fetchResult.toSuspendFuncStatusInfo())
        }
    }

}