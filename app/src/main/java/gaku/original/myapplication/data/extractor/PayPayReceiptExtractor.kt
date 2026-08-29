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

        /* Mask the image */
        val maskedBitmap = bitmap.maskBitmapArea(
            config.mask.widthPercent,
            config.mask.heightPercent,
            leftPercent = 0.0,
            topPercent = 0.0
        )

        val ocrResult = ocrService.runOcr(maskedBitmap)

        /* Parse here */
        Timber.d("ocrResult = ${ocrResult.lines}")


        return AppResult.Success(
            SentData.Expense(
                datetime = null,
                amount = null,
                storeName = null,
                bitmap = maskedBitmap
            )
        )
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

    val left = (width * leftPercent).toInt()
    val top = (height * topPercent).toInt()
    val rectWidth = (width * widthPercent).toInt()
    val rectHeight = (height * heightPercent).toInt()

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
