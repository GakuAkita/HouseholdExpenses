package gaku.original.myapplication.ui.screens.receiver.paypayReceiptMask

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.repository.paypayReceipt.PayPayReceiptConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class PayPayReceiptMaskRatioAdjustUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val leftRatio: Float = 0f,/* Not percent!! */
    val topRatio: Float = 0f,/* Not percent!! */
    val bitmap: Bitmap? = null
)

class PayPayReceiptMaskRatioAdjustViewModel(
    private val imagePath: String,
    private val payPayReceiptConfigRepository: PayPayReceiptConfigRepository
) : ViewModel() {
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
                _uiState.update {
                    it.copy(
                        bitmap = bitmap,
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


}