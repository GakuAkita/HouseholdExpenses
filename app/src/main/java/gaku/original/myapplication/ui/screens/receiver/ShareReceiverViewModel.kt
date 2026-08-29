package gaku.original.myapplication.ui.screens.receiver

import android.graphics.Bitmap
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.SharedData
import gaku.original.myapplication.common.AppResult
import gaku.original.myapplication.data.extractor.Extractor
import gaku.original.myapplication.data.extractor.ExtractorError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime

data class ShareReceiverUiState(
    val sharedData: SharedData? = null,
    val maskedBitmap: Bitmap? = null,
    val isLoading: Boolean = false
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
                Timber.d("analyzeSharedData() called.")
                analyzeSharedData(sharedData)
            } catch (e: Exception) {
                Timber.e(e)
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
                val imageUri = sharedData.imagePath
                if (imageUri == null) {
                    throw Exception("Image path is null")
                }

                Timber.d("package name:${packageName}")
                if (packageName.contains("jp.ne.paypay.android")) {
                    /* PayPay */
                    val result = paypayExtractor.extract(imageUri.toUri())
                    if (result is AppResult.Success) {
                        Timber.d("${result.value}")
                        when (val data = result.value) {
                            is SentData.Expense -> {
                                _uiState.update {
                                    it.copy(
                                        maskedBitmap = data.bitmap
                                    )
                                }
                            }
                        }
                    } else if (result is AppResult.Failure) {
                        if (result.error is ExtractorError.MaskNotSetError) {
                            /* open new screen to set mask parameters*/
                        }
                    }
                } else {
                    /* エラー */
                    Timber.d("Nothing")
                }
            }

            is SharedData.Unknown -> {
                /* finish?? */
                Timber.d("Unknown shared data")
            }
        }
    }

    override fun onCleared() {
        Timber.d("onCleared() called.${hashCode()}")
        super.onCleared()
    }
}

sealed interface SentData {
    data class Expense(
        val datetime: LocalDateTime?,
        val amount: Long?,
        val storeName: String?,
        val bitmap: Bitmap? = null
    ) : SentData
}