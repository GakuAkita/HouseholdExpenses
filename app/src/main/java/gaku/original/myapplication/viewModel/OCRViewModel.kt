package gaku.original.myapplication.viewModel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.FuncStatusInfo
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.getDefaultExpense
import gaku.original.myapplication.parser.PayPayReceiptParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class OCRViewModel @Inject constructor(
    private val sharedImageViewModel: SharedImageViewModel
) : ViewModel() {
    val className: String = this::class.java.simpleName

    override fun onCleared() {
        super.onCleared()
        Log.d(className, "OCRViewModel Cleared!!")
        sharedImageViewModel.clearSharedImageUri()
    }

    private val _ocrReading = MutableStateFlow(false)
    val ocrReading: StateFlow<Boolean> = _ocrReading

    private val _ocrResult = MutableStateFlow<Text?>(null)
    val ocrResult: StateFlow<Text?> get() = _ocrResult

    private val _ocrUri = MutableStateFlow<Uri?>(null)
    val ocrUri: StateFlow<Uri?> = _ocrUri.asStateFlow()

    private val _uriUpdatedTimestamp = MutableStateFlow(0L)
    val uriUpdatedTimestamp: StateFlow<Long> = _uriUpdatedTimestamp

    private val _extractedExpense = MutableStateFlow(getDefaultExpense())
    val extractedExpense: StateFlow<Expense> = _extractedExpense

    init {
        viewModelScope.launch {
            /**
             *  sharedViewModelのuriに更新があったときに検知できるように画面が生きている間は監視しておく
             *  sharedImageは名前が全部一緒なのでcollectされない？？
             *  */
            sharedImageViewModel.updatedTimeStamp.collect { t ->
                _ocrUri.value = sharedImageViewModel.sharedImageUri.value
                _uriUpdatedTimestamp.value = t/* uriはいつも同じ名前なので検知されない。したがって、フラグを見る */
            }
        }
    }

    fun getImageUri(): Uri? {
        return sharedImageViewModel.sharedImageUri.value
    }

    fun runOcr(context: Context, callback: (FuncStatusInfo) -> Unit = {}) {
        _ocrReading.value = true
        val imageUri = getImageUri()
        viewModelScope.launch {
            val result = getOcrResult(context, imageUri)
            if (result is FuncResultWithData.Success) {
                _ocrResult.value = result.data

                /* ここでPayPayの読み込み行うか */
                val paypayParser = PayPayReceiptParser(result.data)
                val paypayResult = paypayParser.parse()
                if (paypayResult is FuncResultWithData.Success) {
                    _extractedExpense.value = paypayResult.data
                    _ocrReading.value = false
                }
            }
            callback(result.toFuncStatusInfo())
        }
    }

    suspend fun getOcrResult(context: Context, imageUri: Uri?): FuncResultWithData<Text?> {
        if (imageUri == null) {
            return FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                "imageUriが入っていません"
            )
        }
        val bitmap = loadBitmapFromUri(context, imageUri)
            ?: return FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                "bitmap is null"
            )
        val image = InputImage.fromBitmap(bitmap, 0)

        val recognizer =
            TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())

        /* タイムアウト作った法が良いかな */
        return try {
            val visionText = recognizer.process(image).await()
            FuncResultWithData.Success(data = visionText)
        } catch (e: Exception) {
            FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = e.message ?: "unknown error"
            )
        }
    }

    private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            null
        }
    }
}


