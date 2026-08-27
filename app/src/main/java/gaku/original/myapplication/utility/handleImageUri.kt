package gaku.original.myapplication.utility

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.FuncResultWithData

/**
 * 渡されたuriからbitmapを読み込む
 */
fun loadBitmapFromUri(context: Context, uri: Uri): FuncResultWithData<Bitmap> {
    return try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val bitmap = BitmapFactory.decodeStream(stream)
            if (bitmap != null) {
                FuncResultWithData.Success(
                    data = bitmap,
                )
            } else {
                FuncResultWithData.Failure.GenericFailure(
                    status = FuncStatus.FAILED,
                    errorMessage = "Failed to decode bitmap"
                )
            }
        } ?: FuncResultWithData.Failure.GenericFailure(
            status = FuncStatus.FAILED,
            errorMessage = "Failed to open input stream"
        )
    } catch (e: SecurityException) {
        e.printStackTrace()
        FuncResultWithData.Failure.GenericFailure(
            status = FuncStatus.FAILED,
            errorMessage = "SecurityException:${e.message}"
        )
    } catch (e: Exception) {
        e.printStackTrace()
        FuncResultWithData.Failure.GenericFailure(
            status = FuncStatus.FAILED,
            errorMessage = "Exception:${e.message}"
        )
    }
}
