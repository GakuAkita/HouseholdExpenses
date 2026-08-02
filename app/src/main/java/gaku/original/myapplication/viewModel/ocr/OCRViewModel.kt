package gaku.original.myapplication.viewModel.ocr

import android.content.Context
import android.graphics.Bitmap
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
import gaku.original.myapplication.data.mapFailure
import gaku.original.myapplication.data.repository.PrefKeys
import gaku.original.myapplication.data.repository.SharedPreferencesRepository
import gaku.original.myapplication.parser.PayPayReceiptOCRParser
import gaku.original.myapplication.utility.loadBitmapFromUri
import gaku.original.myapplication.utility.maskBitmapTopLeftArea
import gaku.original.myapplication.viewModel.shared.SharedImageViewModel
import gaku.original.myapplication.viewModel.shared.TemporaryExpenseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * 場合によっては別の場所に移動
 */
data class OcrResultData(
    val text: Text,
    val imageWidth: Int,
    val imageHeight: Int
)

@HiltViewModel
class OCRViewModel @Inject constructor(
    private val sharedImageViewModel: SharedImageViewModel,
    private val tmpExpenseViewModel: TemporaryExpenseViewModel,
    private val prefRepository: SharedPreferencesRepository
) : ViewModel() {
    val className: String = this::class.java.simpleName

    override fun onCleared() {
        super.onCleared()
        Log.d(className, "OCRViewModel Cleared!!")
        sharedImageViewModel.clearSharedImageData()
    }

    private val _ocrReading = MutableStateFlow(false)
    val ocrReading: StateFlow<Boolean> = _ocrReading

    private val _ocrResult = MutableStateFlow<OcrResultData?>(null)
    val ocrResult: StateFlow<OcrResultData?> get() = _ocrResult

    private val _sharedImageData = MutableStateFlow(sharedImageViewModel.sharedImageData.value)
    val sharedImageData: StateFlow<SharedImageData?> get() = _sharedImageData

    private val _extractedExpense = MutableStateFlow(getDefaultExpense())
    val extractedExpense: StateFlow<Expense> = _extractedExpense

    private val _maskedBitmap = MutableStateFlow<Bitmap?>(null)
    val maskedBitmap: StateFlow<Bitmap?> = _maskedBitmap

    private val _isLeftRatioSet = MutableStateFlow(false)
    val isLeftRatioSet: StateFlow<Boolean> = _isLeftRatioSet
    private val _isTopRatioSet = MutableStateFlow(false)
    val isTopRatioSet: StateFlow<Boolean> = _isTopRatioSet

    /* レシートの左端から何%マスキングするか */
    private val _leftRatio = MutableStateFlow<Float?>(null)
    val leftRatio: StateFlow<Float?> = _leftRatio

    /* レシートの上端から何%マスキングするか */
    private val _topRatio = MutableStateFlow<Float?>(null)
    val topRatio: StateFlow<Float?> = _topRatio

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

//                val exclusionRatio = pref.getFloat(
//                    PrefKeys.EXCLUSION_RATIO_FROM_SCREEN_LEFT_FOR_PAYPAY_RECEIPT_OCR,
//                    0.22f
//                )
                val exclusionRatio = 0.28f
                /* ここでPayPayの読み込み行うか */
                val paypayParser = PayPayReceiptOCRParser(result.data?.text)
                val imageWidth = result.data?.imageWidth ?: 0
                val paypayResult = paypayParser.parse(
                    exclusionRatioFromScreenLeft = exclusionRatio,
                    imageWidth = imageWidth
                )
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

    suspend fun getOcrResult(context: Context, imageUri: Uri?): FuncResultWithData<OcrResultData?> {
        if (imageUri == null) {
            return FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                "imageUriが入っていません"
            )
        }
        val bitmapRet = loadBitmapFromUri(context, imageUri)
        if (bitmapRet !is FuncResultWithData.Success) {
            return bitmapRet.mapFailure()
        }
        val bitmap = bitmapRet.data
        /* あらかじめロゴの部分を削っておく */
        val leftRatio = prefRepository.getFloat(PrefKeys.PAYPAY_RECEIPT_LEFT_MASK_RATIO)
        val topRatio = prefRepository.getFloat(PrefKeys.PAYPAY_RECEIPT_TOP_MASK_RATIO)
        val masked = maskBitmapTopLeftArea(
            bitmap,
            widthPercent = leftRatio,
            heightPercent = topRatio
        )
        /* マスクしたbitmapをUI上に表示する */
        _maskedBitmap.value = masked

        val imageWidth = masked.width
        val imageHeight = masked.height
        val image = InputImage.fromBitmap(masked, 0)
        Log.d(className, "Image width=${imageWidth}　height=${imageHeight}")

        val recognizer =
            TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())

        /* タイムアウト作った法が良いかな */
        return try {
            val visionText = recognizer.process(image).await()
            FuncResultWithData.Success(
                data = OcrResultData(
                    text = visionText,
                    imageWidth = imageWidth,
                    imageHeight = imageHeight
                )
            )
        } catch (e: Exception) {
            FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = e.message ?: "unknown error"
            )
        }
    }


    fun copyReadExpenseToTmpExpense() {
        tmpExpenseViewModel.resetTmpExpenseList()
        tmpExpenseViewModel.updateTmpExpense(_extractedExpense.value)
    }

    fun clearSharedImageData() {
        sharedImageViewModel.clearSharedImageData()
    }

    /**************bitmapのマスキング関連の設定*******************/
    fun loadIsMaskRatioSet() {
        _isLeftRatioSet.value = prefRepository.hasKey(PrefKeys.PAYPAY_RECEIPT_LEFT_MASK_RATIO)
        _isTopRatioSet.value = prefRepository.hasKey(PrefKeys.PAYPAY_RECEIPT_TOP_MASK_RATIO)
    }

    fun loadMaskRatio() {
        _leftRatio.value = prefRepository.getFloat(PrefKeys.PAYPAY_RECEIPT_LEFT_MASK_RATIO)
        _topRatio.value = prefRepository.getFloat(PrefKeys.PAYPAY_RECEIPT_TOP_MASK_RATIO)
    }
}


