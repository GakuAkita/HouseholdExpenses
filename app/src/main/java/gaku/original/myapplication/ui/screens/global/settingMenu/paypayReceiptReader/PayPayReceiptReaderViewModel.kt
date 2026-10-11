package gaku.original.myapplication.ui.screens.global.settingMenu.paypayReceiptReader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.repository.paypayReceipt.MaskConfig
import gaku.original.myapplication.data.repository.paypayReceipt.PayPayReceiptConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class PayPayReceiptReaderUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    /* Size of the top-left masked area, in percent of the receipt image. null means not set. */
    val maskWidthPercent: Float? = null,
    val maskHeightPercent: Float? = null,
    val isLoadError: Boolean = false
) {
    val isMaskSet: Boolean
        get() = maskWidthPercent != null && maskHeightPercent != null
}

class PayPayReceiptReaderViewModel(
    private val payPayReceiptConfigRepository: PayPayReceiptConfigRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PayPayReceiptReaderUiState())
    val uiState get() = _uiState.asStateFlow()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyApplication
                val container = app.appContainer
                val session = container.sessionContainer!!
                PayPayReceiptReaderViewModel(
                    session.payPayReceiptConfigRepository
                )
            }
        }
    }

    init {
        Timber.d("Created. ${hashCode()}")

        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isLoading = true
                    )
                }
                val ocrConfig = payPayReceiptConfigRepository.getOCRSetting()
                when (val mask = ocrConfig.mask) {
                    is MaskConfig.Percent -> {
                        /*
                         * The mask always starts at the top-left corner (left/top = 0),
                         * so width/height are the values the user adjusted.
                         */
                        _uiState.update {
                            it.copy(
                                maskWidthPercent = mask.widthPercent?.toFloat(),
                                maskHeightPercent = mask.heightPercent?.toFloat()
                            )
                        }
                    }
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadError = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = e.message,
                        isLoadError = true
                    )
                }
            }
        }
    }

    fun resetSetting() {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isLoading = true
                    )
                }
                payPayReceiptConfigRepository.clearOCRSetting()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Masking setting was reset",
                        maskWidthPercent = null,
                        maskHeightPercent = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = e.message
                    )
                }
            }
        }
    }

    fun onMessageShown() {
        _uiState.value = _uiState.value.copy(
            message = null
        )
    }

    override fun onCleared() {
        Timber.d("Cleared. ${hashCode()}")
        super.onCleared()
    }
}
