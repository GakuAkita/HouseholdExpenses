package gaku.original.myapplication.ui.screens.receiver.paypayReceiptMask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.repository.paypayReceipt.PayPayReceiptConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

data class PayPayReceiptMaskRatioAdjustUiState(
    val isLoading: Boolean = false,
    val leftRatio: Float = 0f,
    val topRatio: Float = 0f
)

class PayPayReceiptMaskRatioAdjustViewModel(
    private val payPayReceiptConfigRepository: PayPayReceiptConfigRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PayPayReceiptMaskRatioAdjustUiState())
    val uiState = _uiState.asStateFlow()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyApplication
                val container = app.appContainer
                val session = container.sessionContainer!!
                PayPayReceiptMaskRatioAdjustViewModel(
                    payPayReceiptConfigRepository = session.payPayReceiptConfigRepository
                )
            }
        }
    }

    init {
        Timber.d("Created.${hashCode()}")
    }


}