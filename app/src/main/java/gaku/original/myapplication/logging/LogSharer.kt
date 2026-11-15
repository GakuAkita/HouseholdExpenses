package gaku.original.myapplication.logging

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.FileProvider
import gaku.original.myapplication.BuildConfig
import java.io.File

/**
 * Joins the rotated log files into one text file in the cache and builds a share intent for it.
 * Returns null when nothing has been logged yet.
 */
fun createLogShareIntent(context: Context, tree: FileLoggingTree): Intent? {
    val shareDir = File(context.cacheDir, SHARED_LOG_DIR).apply { mkdirs() }
    val out = File(shareDir, "household-expenses-log.txt")

    val wrote = tree.onWriterThread { logFile ->
        val files = logFile.files()
        if (files.isEmpty()) return@onWriterThread false
        out.outputStream().use { stream ->
            stream.write(header().toByteArray(Charsets.UTF_8))
            files.forEach { file -> file.inputStream().use { it.copyTo(stream) } }
        }
        true
    }
    if (!wrote) return null

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", out)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "HouseholdExpenses log")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return Intent.createChooser(send, "Share log")
}

private fun header() = buildString {
    appendLine("App: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) ${BuildConfig.BUILD_TYPE}")
    appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
    appendLine()
}

/** Must match the cache-path in res/xml/file_paths.xml. */
private const val SHARED_LOG_DIR = "shared_logs"
