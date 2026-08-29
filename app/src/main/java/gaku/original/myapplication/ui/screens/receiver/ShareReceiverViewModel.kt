package gaku.original.myapplication.ui.screens.receiver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.SharedData
import gaku.original.myapplication.data.extraction.extractor.Extractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

data class ShareReceiverUiState(
    val sharedData: SharedData? = null
)

class ShareReceiverViewModel(
    private val sharedData: SharedData,
    private val paypayExtractor: Extractor
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShareReceiverUiState())
    val uiState = _uiState.asStateFlow()

    companion object {
        fun Factory(sharedData: SharedData): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyApplication
                val container = app.appContainer
                val session = container.sessionContainer!!

                ShareReceiverViewModel(
                    sharedData,
                    paypayExtractor = session.payPayReceiptExtractor
                )
            }
        }
    }

    init {
        Timber.d("init() called.${hashCode()}")
        _uiState.value = ShareReceiverUiState(
            sharedData
        )

        viewModelScope.launch {
            try {
                analyzeSharedData(sharedData)
            } catch (e: Exception) {

            }
        }
    }

    suspend fun analyzeSharedData(sharedData: SharedData) {
        when (sharedData) {
            is SharedData.Image -> {
                val packageName = sharedData.packageName
                if (packageName == null) {
                    throw Exception("Package name is null")
                }

                if (packageName.contains("jp.co.pay.android")) {
                    /* PayPay */
                } else {
                    /* エラー */
                }
            }

            is SharedData.Unknown -> {
                /* finish?? */
            }
        }
    }

    override fun onCleared() {
        Timber.d("onCleared() called.${hashCode()}")
        super.onCleared()
    }
}

sealed interface SentData {
    data class Expense(val datetime: String?, val amount: Long?, val storeName: String?) : SentData
}