package gaku.original.myapplication.utility

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri

/**
 * 渡されたuriからbitmapを読み込む
 */
fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    } catch (e: SecurityException) {
        e.printStackTrace()
        null
    }
}

fun maskBitmapArea(
    source: Bitmap,
    widthPercent: Float,
    heightPercent: Float,
    leftPercent: Float = 0f,
    topPercent: Float = 0f,
    color: Int = Color.WHITE,              // 塗りつぶす色
    alpha: Int = 255,                      // 不透明度（0〜255）
    style: Paint.Style = Paint.Style.FILL  // 塗りつぶし or 枠線
): Bitmap {
    // 編集可能なコピーを作成
    val mutable = source.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(mutable)
    val paint = Paint().apply {
        this.color = color
        this.alpha = alpha
        this.style = style
        isAntiAlias = true
    }

    val width = source.width
    val height = source.height

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
private fun maskBitmapTopLeftArea(
    source: Bitmap,
    widthPercent: Float,
    heightPercent: Float,
    leftPercent: Float = 0f,
    topPercent: Float = 0f
): Bitmap {
    return maskBitmapArea(
        source = source,
        widthPercent = widthPercent,
        heightPercent = heightPercent,
        leftPercent = leftPercent,
        topPercent = topPercent,
        color = Color.WHITE, // 既存仕様どおり白
        alpha = 255
    )
}
