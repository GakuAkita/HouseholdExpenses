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
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.getDefaultExpense
import gaku.original.myapplication.utility.AppTimeZone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class OCRViewModel @Inject constructor(
    private val sharedImageViewModel: SharedImageViewModel
) : ViewModel() {
    val className = this::class.java.simpleName

    override fun onCleared() {
        super.onCleared()
        Log.d(className, "OCRViewModel Cleared!!")
        sharedImageViewModel.clearSharedImageUri()
    }

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

    fun createExpenseByPayPayReceipt(context: Context, callback: () -> Unit = {}) {
        runOcr(context) {
            _extractedExpense.value = parsePayPayReceipt(ocrResult.value)
            callback()
        }
    }

    fun runOcr(context: Context, callback: () -> Unit) {
        val imageUri = getImageUri()
        if (imageUri != null) {
            runOcrPayPay(context, imageUri) {
                callback()
            }
        }
    }

    suspend fun getOcrResult(context: Context, imageUri: Uri): FuncResultWithData<Text?> {
        val bitmap = loadBitmapFromUri(context, imageUri)
            ?: return FuncResultWithData.Failure.GenericFailure(
                status = SuspendFuncStatus.FAILED,
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
                status = SuspendFuncStatus.FAILED,
                errorMessage = e.message ?: "unknown error"
            )
        }
    }

    /** OCR を実行するメソッド */
    /* 基本PayPayの共有画像のみ対応 */
    fun runOcrPayPay(
        context: Context,
        imageUri: Uri,
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = getOcrResult(context, imageUri)
            if (result is FuncResultWithData.Success) {
                _ocrResult.value = result.data
            }
            callback(result.toSuspendFuncStatusInfo())
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

/**
 *
 */
fun extractDateFromPayPay(text: String): String? {
    val dateJaRegex = """(\d{4}年\d{1,2}月\d{1,2}日\s*\d{1,2}時\d{1,2}分)""".toRegex()
    val dateEnRegex = """\d{4}/\d{1,2}/\d{1,2}\s+\d{1,2}:\d{2}""".toRegex()

    dateJaRegex.find(text)?.let {
        /* yyyy年mm月dd日 HH時MM分をLocalDateTimeに変換して、それをUTC文字列に変える */
        val formatter = DateTimeFormatter.ofPattern("yyyy年M月d日 H時m分")
        val localDateTime = LocalDateTime.parse(it.value, formatter)
        val isoStr = AppTimeZone.localDateTimeToIsoString(localDateTime)

        return isoStr
    }

    dateEnRegex.find(text)?.let {
        val formatter = DateTimeFormatter.ofPattern("yyyy/M/d/ H:m")
        val localDateTime = LocalDateTime.parse(it.value, formatter)
        val isoStr = AppTimeZone.localDateTimeToIsoString(localDateTime)
        return isoStr
    }

    /* 検知できなかった */
    return null
}

fun extractAmountFromPayPay(text: String): Long? {
    /* 日本語 */
    val amountJaRegex = """(\d{1,3}(,\d{3})*円)""".toRegex()

    /* 英語 */
    val amountEnRegex = """\d{1,3}(,\d{3})*\s*yen""".toRegex()

    amountJaRegex.find(text)?.let {
        return it.value.replace(",", "").replace("円", "").toLong()
    }

    amountEnRegex.find(text)?.let {
        return it.value.replace(",", "").replace("yen", "").toLong()
    }

    return null
}

/**
 * OCRで読み取った文字列からまず日時と金額を抜き出す(PayPay)
 * 読み取りに失敗するパターンもあるから、
 */
/**
 * 英語と日本語の場合で違うが、
 * 日本語の場合は、まず年月日を見つけてくる その後に続く数値を見つける
 * さらにその次に金額があるので、取ってくる
 */
fun parsePayPayReceipt(ocrText: String): Expense {
    /* 日本語のとき"時"が"B時"と読まれることがあった */
    /* こういうのを増やして精度上げるしかないか、、 */
    var processedText = ocrText.replace("B時", "時")

    /* 改行で行に分割して前後の空白を除去 */
    val lines = processedText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }


    var foundDate: Boolean = false
    var foundAmount: Boolean = false

    val expense = getDefaultExpense()

    lines.forEach { line ->
    }

    return expense
}


