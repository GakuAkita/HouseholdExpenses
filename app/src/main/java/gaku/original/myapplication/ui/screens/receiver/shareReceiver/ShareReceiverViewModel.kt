package gaku.original.myapplication.ui.screens.receiver.shareReceiver

import android.content.Context
import android.graphics.Bitmap
import android.os.Parcelable
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
import kotlinx.parcelize.Parcelize
import timber.log.Timber

data class ShareReceiverUiState(
    val sentData: SentData? = null,
    val isLoading: Boolean = false,
    val message: String? = null,
    val bitmap: Bitmap? = null
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

    fun onMessageShown() {
        _uiState.update {
            it.copy(
                message = null
            )
        }
    }

    init {
        Timber.d("init() called.${hashCode()}")
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isLoading = true
                    )
                }
                Timber.d("analyzeSharedData() called.")
                analyzeSharedData(sharedData)
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.update {
                    it.copy(
                        message = e.message
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(
                        isLoading = false
                    )
                }
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
                        _uiState.update {
                            it.copy(
                                sentData = result.value.sentData,
                                bitmap = result.value.bitmap
                            )
                        }
                    } else if (result is AppResult.Failure) {
                        if (result.error is ExtractorError.MaskNotSetError) {
                            /* open new screen to set mask parameters*/
                        }
                    }
                } else {
                    /* エラー */
                    _uiState.update {
                        it.copy(
                            message = "This package is not supported"
                        )
                    }
                }
            }

            is SharedData.Unknown -> {
                /* finish?? */
                Timber.d("Unknown shared data")
            }
        }
    }

    fun onAddExpenseClick(context: Context) {

    }

    override fun onCleared() {
        Timber.d("onCleared() called.${hashCode()}")
        super.onCleared()
    }
}

@Parcelize
sealed interface SentData : Parcelable {
    @Parcelize
    data class Expense(
        val datetime: String?,
        val amount: Long?,
        val storeName: String?,
    ) : SentData, Parcelable
}