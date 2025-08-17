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
import gaku.original.myapplication.data.dataClass.SharedImageData
import gaku.original.myapplication.data.dataClass.getDefaultExpense
import gaku.original.myapplication.parser.PayPayReceiptOCRParser
import gaku.original.myapplication.viewModel.shared.SharedImageViewModel
import gaku.original.myapplication.viewModel.shared.TemporaryExpenseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class OCRViewModel @Inject constructor(
    private val sharedImageViewModel: SharedImageViewModel,
    private val tmpExpenseViewModel: TemporaryExpenseViewModel,
) : ViewModel() {
    val className: String = this::class.java.simpleName

    override fun onCleared() {
        super.onCleared()
        Log.d(className, "OCRViewModel Cleared!!")
        sharedImageViewModel.clearSharedImageData()
    }

    private val _ocrReading = MutableStateFlow(false)
    val ocrReading: StateFlow<Boolean> = _ocrReading

    private val _ocrResult = MutableStateFlow<Text?>(null)
    val ocrResult: StateFlow<Text?> get() = _ocrResult

    private val _sharedImageData = MutableStateFlow(sharedImageViewModel.sharedImageData.value)
    val sharedImageData: StateFlow<SharedImageData?> get() = _sharedImageData

    private val _extractedExpense = MutableStateFlow(getDefaultExpense())
    val extractedExpense: StateFlow<Expense> = _extractedExpense

    init {
        viewModelScope.launch {
            /**
             *  sharedViewModelのuriに更新があったときに検知できるように画面が生きている間は監視しておく
             *  sharedImageは名前が全部一緒なのでcollectされない？？
             *  */
            sharedImageViewModel.sharedImageData.collect { t ->
                _sharedImageData.value = sharedImageViewModel.sharedImageData.value
            }
        }
    }

    fun getImageUri(): Uri? {
        return sharedImageViewModel.sharedImageData.value?.imageUri
    }

    fun runOcr(
        context: Context,
        callback: (FuncStatusInfo) -> Unit = {}
    ) {
        _ocrReading.value = true
        val imageUri = getImageUri()
        viewModelScope.launch {
            val result = getOcrResult(context, imageUri)
            if (result is FuncResultWithData.Success) {
                _ocrResult.value = result.data

                /* ここでPayPayの読み込み行うか */
                val paypayParser = PayPayReceiptOCRParser(result.data)
                val paypayResult = paypayParser.parse()
                if (paypayResult is FuncResultWithData.Success) {
                    _extractedExpense.value = paypayResult.data
                } else if (paypayResult is FuncResultWithData.Warning) {
                    _extractedExpense.value = paypayResult.data
                }
                Log.d(
                    className,
                    "Created Expense: ${_extractedExpense.value}　paypayParse result:${paypayResult.toFuncStatusInfo()}"
                )
                callback(paypayResult.toFuncStatusInfo())
            } else {
                callback(result.toFuncStatusInfo())
            }
            _ocrReading.value = false
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

    fun copyReadExpenseToTmpExpense() {
        tmpExpenseViewModel.resetTmpExpenseList()
        tmpExpenseViewModel.updateTmpExpense(_extractedExpense.value)
    }

    fun clearSharedImageData() {
        sharedImageViewModel.clearSharedImageData()
    }
}


