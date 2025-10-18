package gaku.original.myapplication.utility

import android.content.Context
import android.net.Uri
import java.io.File

fun copyUriToCache(context: Context, uri: Uri): Uri? {
    return try {
        // ContentResolver で元の URI を開く
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null

        // キャッシュディレクトリに新しいファイルを作る
        val file = File(context.cacheDir, "shared_image.jpg")

        // コピー
        inputStream.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        // 自分のアプリの URI を返す
        Uri.fromFile(file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}