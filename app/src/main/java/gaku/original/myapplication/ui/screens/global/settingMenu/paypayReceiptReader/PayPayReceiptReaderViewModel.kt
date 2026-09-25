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
import gaku.original.myapplication.data.repository.paypayReceipt.PayPayReceiptOCRSetting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class PayPayReceiptReaderUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val topPercent: Float? = null,
    val leftPercent: Float? = null,
    val isLoadError: Boolean = false
)

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
                        _uiState.update {
                            it.copy(
                                topPercent = mask.topPercent?.toFloat(),
                                leftPercent = mask.leftPercent?.toFloat()
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
                val setting = PayPayReceiptOCRSetting(
                    mask = MaskConfig.Percent(
                        widthPercent = null,
                        heightPercent = null,
                        topPercent = null,
                        leftPercent = null
                    )
                )
                payPayReceiptConfigRepository.saveOCRSetting(
                    setting
                )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Reset Success",
                        topPercent = null,
                        leftPercent = null
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