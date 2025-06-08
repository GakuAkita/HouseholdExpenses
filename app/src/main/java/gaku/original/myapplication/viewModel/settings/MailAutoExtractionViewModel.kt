package gaku.original.myapplication.viewModel.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.FirestoreRepository.CategoryFirestoreRepository
import gaku.original.myapplication.data.FirestoreRepository.MailAutoExtractionFirestoreRepository
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.MailAutoExtractionCommon
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MailAutoExtractionViewModel @Inject constructor(
    private val categoryFirestoreRepository: CategoryFirestoreRepository,
    private val mailAutoExtractionFirestoreRepository: MailAutoExtractionFirestoreRepository
) : ViewModel() {
    val className: String = this::class.simpleName ?: "UnableToGetClassName"

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
}