package gaku.original.myapplication.ui.screens.receiver.paypayReceiptMask

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.common.AppResult
import gaku.original.myapplication.data.extractor.ExtractedData
import gaku.original.myapplication.data.extractor.ExtractorError
import gaku.original.myapplication.data.extractor.paypayReceipt.PayPayReceiptValidator
import gaku.original.myapplication.data.extractor.paypayReceipt.maskBitmapArea
import gaku.original.myapplication.data.repository.paypayReceipt.MaskConfig
import gaku.original.myapplication.data.repository.paypayReceipt.PayPayReceiptConfigRepository
import gaku.original.myapplication.ui.screens.receiver.shareReceiver.SentData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class PayPayReceiptMaskRatioAdjustUiState(
    val isLoading: Boolean = false,
    val isValidating: Boolean = false,
    val message: String? = null,
    val leftRatio: Float = 0.05f,/* Not percent!! */
    val topRatio: Float = 0.05f,/* Not percent!! */
    val originalBitmap: Bitmap? = null,
    val bitmap: Bitmap? = null,

    val showConfirm: Boolean = false,
    val extractResult: ExtractedData? = null
)

class PayPayReceiptMaskRatioAdjustViewModel(
    private val imagePath: String,
    private val payPayReceiptValidator: PayPayReceiptValidator,
    private val payPayReceiptConfigRepository: PayPayReceiptConfigRepository
) : ViewModel() {
    private val hidingColor = Color.RED

    private val _uiState = MutableStateFlow(PayPayReceiptMaskRatioAdjustUiState())
    val uiState = _uiState.asStateFlow()

    companion object {
        fun Factory(imagePath: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyApplication
                val container = app.appContainer
                val session = container.sessionContainer!!
                Timber.d("imagePath:${imagePath}")
                PayPayReceiptMaskRatioAdjustViewModel(
                    imagePath,
                    payPayReceiptValidator = session.payPayReceiptValidator,
                    payPayReceiptConfigRepository = session.payPayReceiptConfigRepository
                )
            }
        }
    }

    init {
        Timber.d("Created.${hashCode()}")
        ConvertUriToBitmap(imagePath)
    }

    fun onMessageShown() {
        _uiState.update {
            it.copy(
                message = null
            )
        }
    }

    private fun ConvertUriToBitmap(imagePath: String) {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isLoading = true
                    )
                }

                val bitmap = BitmapFactory.decodeFile(imagePath)
                _uiState.update { state ->
                    state.copy(
                        originalBitmap = bitmap,
                        bitmap = bitmap.maskBitmapArea(
                            widthPercent = state.leftRatio.toDouble() * 100,
                            heightPercent = state.topRatio.toDouble() * 100,
                            /* always start from the top-left corner */
                            leftPercent = 0.0,
                            topPercent = 0.0,
                            color = hidingColor
                        ),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        message = e.toString(),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onLeftRatioChane(leftRatio: Float) {
        _uiState.update { state ->
            state.copy(
                leftRatio = leftRatio,
                bitmap = state.originalBitmap?.maskBitmapArea(
                    widthPercent = leftRatio.toDouble() * 100,
                    heightPercent = state.topRatio.toDouble() * 100,
                    /* always start from the top-left corner */
                    leftPercent = 0.0,
                    topPercent = 0.0,
                    color = hidingColor
                )
            )
        }
    }

    fun onTopRatioChange(topRatio: Float) {
        _uiState.update { state ->
            state.copy(
                topRatio = topRatio,
                bitmap = state.originalBitmap?.maskBitmapArea(
                    widthPercent = state.leftRatio.toDouble() * 100,
                    heightPercent = topRatio.toDouble() * 100,
                    /* always start from the top-left corner */
                    leftPercent = 0.0,
                    topPercent = 0.0,
                    color = hidingColor
                )
            )
        }
    }

    fun onFABClick() {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isValidating = true
                    )
                }
                val bitmap = _uiState.value.bitmap ?: throw Exception("Bitmap is not set")
                val percentConfig = MaskConfig.Percent(
                    widthPercent = _uiState.value.leftRatio.toDouble() * 100,
                    heightPercent = _uiState.value.topRatio.toDouble() * 100,
                    topPercent = 0.0,
                    leftPercent = 0.0
                )
                when (val result = payPayReceiptValidator.validate(bitmap, percentConfig)) {
                    is AppResult.Success -> {
                        Timber.d("Extract Success!!")
                        /* input values */
                        val data = result.value.sentData
                        when (data) {
                            is SentData.Expense -> {
                                _uiState.update {
                                    it.copy(
                                        extractResult = result.value,
                                        showConfirm = true
                                    )
                                }
                            }
                        }
                    }

                    is AppResult.Failure -> {
                        val error = result.error
                        when (error) {
                            is ExtractorError.NoStringFoundError -> {
                                _uiState.update {
                                    it.copy(
                                        message = "No string found. Masking too much??"
                                    )
                                }
                            }

                            is ExtractorError.MaskNotSetError -> {
                                throw Exception("Mask not set error. This never happens. Please contact the developer")
                            }
                        }
                    }
                }
                _uiState.update {
                    it.copy(
                        isValidating = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        message = e.toString(),
                        isValidating = false
                    )
                }
            }
        }
    }

    fun onDismissDialog() {
        _uiState.update {
            it.copy(
                showConfirm = false
            )
        }
    }
}