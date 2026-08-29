package gaku.original.myapplication.data.extractor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import gaku.original.myapplication.common.AppResult
import gaku.original.myapplication.data.repository.paypayReceipt.MaskConfig
import gaku.original.myapplication.data.repository.paypayReceipt.PayPayReceiptConfigRepository
import gaku.original.myapplication.service.ocr.OcrService
import gaku.original.myapplication.ui.screens.receiver.SentData
import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PayPayReceiptExtractor(
    private val context: Context,/* This should be abstracted, but it's too much work. I just pass context. */
    private val paypayReceiptConfigRepository: PayPayReceiptConfigRepository,
    private val ocrService: OcrService
) : Extractor {
    override suspend fun extract(image: Uri): AppResult<SentData.Expense, ExtractorError> {
        val bitmap: Bitmap = context.contentResolver.openInputStream(image)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        } ?: throw Exception("Failed to open input stream")

        val config = paypayReceiptConfigRepository.getOCRSetting()
        if (config.mask !is MaskConfig.Percent) {
            throw Exception("Coding Error: Invalid mask config. PayPay should set by percent")
        }

        if (config.mask.widthPercent == null || config.mask.heightPercent == null) {
            return AppResult.Failure(ExtractorError.MaskNotSetError)
        }

        Timber.d("widthPercent = ${config.mask.widthPercent} heightPercent = ${config.mask.heightPercent}")

        /* Mask the image */
        val maskedBitmap = bitmap.maskBitmapArea(
            config.mask.widthPercent,
            config.mask.heightPercent,
            leftPercent = 0.0,
            topPercent = 0.0
        )

        val ocrResult = ocrService.runOcr(maskedBitmap)

        if (ocrResult.lines.isEmpty()) {
            return AppResult.Failure(ExtractorError.NoStringFoundError)
        }

        val lines = ocrResult.lines

        /* Parse here */
        Timber.d("ocrResult = ${lines}")

        /* get datetime first */
        var dtIndex: Int = 0
        var datetime: LocalDateTime? = null
        val storeNamePart = StringBuilder()
        for (i in lines.indices) {
            datetime = extractDate(lines[i])
            if (datetime != null) {
                dtIndex = i
                break
            }
            storeNamePart.append(lines[i])
        }

        var storeName: String? = null
        if (dtIndex < lines.size - 1) {
            storeName = createStoreName(storeNamePart.toString())
        }

        /* get amount */
        var amount: Long? = null
        for (i in dtIndex..lines.size - 1) {
            amount = extractAmount(lines[i])
            if (amount != null) {
                break
            }
        }

        return AppResult.Success(
            SentData.Expense(
                datetime = datetime,
                amount = amount,
                storeName = storeName,
                bitmap = maskedBitmap
            )
        )
    }

    private fun extractDate(text: String): LocalDateTime? {
        // OCRのノイズ対策：B時を時に置換、全角スペースや複数スペースを削除
        val processedT = text.replace("B時", "時").replace("\\s+".toRegex(), " ").trim()

        // 日本語形式：yyyy年M月d日H時m分（スペース有無対応）
        val dateJaRegex = """(\d{4}年\d{1,2}月\d{1,2}日\s*\d{1,2}時\d{1,2}分)""".toRegex()
        dateJaRegex.find(processedT)?.value?.let { jaDate ->
            val normalized = jaDate.replace("\\s+".toRegex(), "") // スペース削除
            val formatter = DateTimeFormatter.ofPattern("yyyy年M月d日H時m分")
            val localDateTime = LocalDateTime.parse(normalized, formatter)
            return localDateTime
        }

        // 英数字形式：yyyy/M/d H:m（スペース有無や時刻くっつき対応）
        val dateEnRegex = """(\d{4}/\d{1,2}/\d{1,2}\s*\d{1,2}:\d{2})""".toRegex()
        dateEnRegex.find(processedT)?.value?.let { enDate ->
            // 時刻の前に必ずスペースを入れる
            val normalized = enDate.replace(Regex("(\\d{1,2}):(\\d{2})"), " $1:$2")
                .replace("\\s+".toRegex(), " ") // 複数スペースを1つに
                .trim()
            val formatter = DateTimeFormatter.ofPattern("yyyy/M/d H:m")
            val localDateTime = LocalDateTime.parse(normalized, formatter)
            return localDateTime
        }

        // どちらにもマッチしなかった場合
        return null
    }

    private fun extractAmount(text: String): Long? {

        /* the symbol besides yen can be recognized as "a". Remove it here */
        val processedT = text.replace("a", "")

        /* 日本語 */
        val amountJaRegex = """(\d{1,3}(,\d{3})*円)""".toRegex()

        /* 英語 */
        val amountEnRegex = """\d{1,3}(,\d{3})*\s*yen""".toRegex()

        amountJaRegex.find(text)?.let {
            return it.value.replace(",", "").replace("円", "").trim().toLongOrNull()
        }

        amountEnRegex.find(text)?.let {
            return it.value.replace(",", "").replace("yen", "").trim().toLongOrNull()
        }

        return null
    }

    private fun createStoreName(text: String): String? {
        /**
         * ダイソー\nフジ東予店
         * のパターンと
         * ハローズ\nハローズ 東予店
         * という感じで2行目に店舗名しか入らないパターンと店名も含むパターンがある
         */

        val textSplit = text.split("\n")
        val name = textSplit.getOrNull(0)
        val storeName = textSplit.getOrNull(1)

        if (name == null && storeName == null) {
            return null
        } else if (name == null) {
            return storeName
        } else if (storeName == null) {
            return name
        } else {
            /* nameもstoreNameも入っている */
        }

        /**
         *  nameもstoreNameも入っているとき、
         *  storeNameにnameが入っていたらそのままnameを返す
         *  */
        if (storeName.contains(name)) {
            return storeName
        }

        /* 空欄は取り除いておく */
        return "${name.replace(" ", "")} ${storeName.replace(" ", "")}"
    }
}

fun Bitmap.maskBitmapArea(
    widthPercent: Double,
    heightPercent: Double,
    leftPercent: Double = 0.0,
    topPercent: Double = 0.0,
    color: Int = Color.WHITE,              // 塗りつぶす色
    alpha: Int = 255,                      // 不透明度（0〜255）
    style: Paint.Style = Paint.Style.FILL  // 塗りつぶし or 枠線
): Bitmap {
    // 編集可能なコピーを作成
    val mutable = this.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(mutable)
    val paint = Paint().apply {
        this.color = color
        this.alpha = alpha
        this.style = style
        isAntiAlias = true
    }

    val width = this.width
    val height = this.height

    val left = (width * leftPercent / 100).toInt()
    val top = (height * topPercent / 100).toInt()
    val rectWidth = (width * widthPercent / 100).toInt()
    val rectHeight = (height * heightPercent / 100).toInt()

    val rect = Rect(left, top, left + rectWidth, top + rectHeight)
    canvas.drawRect(rect, paint)

    return mutable
}


/**
 * ラップしただけ、
 */
fun maskBitmapTopLeftArea(
    source: Bitmap,
    widthPercent: Double,
    heightPercent: Double,
    leftPercent: Double = 0.0,
    topPercent: Double = 0.0
): Bitmap {
    return source.maskBitmapArea(
        widthPercent = widthPercent,
        heightPercent = heightPercent,
        leftPercent = leftPercent,
        topPercent = topPercent,
        color = Color.WHITE, // 既存仕様どおり白
        alpha = 255
    )
}
