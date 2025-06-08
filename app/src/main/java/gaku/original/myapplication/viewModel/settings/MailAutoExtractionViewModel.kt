package gaku.original.myapplication.viewModel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.FirestoreRepository.MailAutoExtractionFirestoreRepository
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.MailAutoExtractionCommon
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MailAutoExtractionViewModel @Inject constructor(
    private val mailAutoExtractionFirestoreRepository: MailAutoExtractionFirestoreRepository
) : ViewModel() {
    
    fun setMailAutoExtractionInternalType(
        instance: MailAutoExtractionCommon,
        callback: (SuspendFuncStatusInfo) -> Unit
    ) {
        viewModelScope.launch {
            mailAutoExtractionFirestoreRepository.setMailAutoExtractionInternalType(
                instance,
                callback = callback
            )
        }
    }
}