package gaku.original.myapplication.data.extraction.extractor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import gaku.original.myapplication.data.repository.maskConfigRepository.MaskConfig
import gaku.original.myapplication.data.repository.maskConfigRepository.MaskConfigRepository
import gaku.original.myapplication.service.ocr.OcrService

class PayPayReceiptExtractor(
    private val context: Context,/* This should be abstracted, but it's too much work. I just pass context. */,
    private val maskConfigRepository: MaskConfigRepository,
    private val ocrService: OcrService
) : Extractor {
    override suspend fun extract(image: Uri) {
        val bitmap: Bitmap = context.contentResolver.openInputStream(image)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        } ?: throw Exception("Failed to open input stream")

        val config = maskConfigRepository.getMaskConfig()
        if (config !is MaskConfig.Percent) {
            throw Exception("Coding Error: Invalid mask config. PayPay should set by percent")
        }

        if (config.widthPercent == null || config.heightPercent == null) {

        }

        /* Mask the image */
        val maskedBitmap = bitmap.maskBitmapArea(config.leftPercent, 30f)
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
