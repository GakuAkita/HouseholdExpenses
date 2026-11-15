package gaku.original.myapplication.logging

import android.util.Log
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Timber tree that keeps logs on the device so they can be shared from Settings later.
 * Only [minPriority] and above are written, to keep the file small and avoid noise.
 *
 * Logs may contain notification text and other personal data. They stay in app-private
 * storage, are excluded from backup, and leave the device only when the user shares them.
 */
class FileLoggingTree(
    private val logFile: RotatingLogFile,
    private val minPriority: Int = Log.INFO,
) : Timber.Tree() {

    private val executor: ExecutorService = Executors.newSingleThreadExecutor { r ->
        Thread(r, "FileLoggingTree").apply { isDaemon = true }
    }
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    override fun isLoggable(tag: String?, priority: Int): Boolean = priority >= minPriority

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Timber already appends the stack trace of t to message.
        val line = formatLine(System.currentTimeMillis(), priority, tag, message)
        executor.execute {
            runCatching { logFile.append(line) }
        }
    }

    /** Runs [block] on the writer thread, after every log already queued. */
    fun <T> onWriterThread(block: (RotatingLogFile) -> T): T =
        executor.submit<T> { block(logFile) }.get()

    private fun formatLine(time: Long, priority: Int, tag: String?, message: String): String {
        val timestamp = synchronized(timeFormat) { timeFormat.format(Date(time)) }
        return "$timestamp ${priorityLetter(priority)}/${tag ?: "-"}: $message"
    }

    private fun priorityLetter(priority: Int) = when (priority) {
        Log.VERBOSE -> "V"
        Log.DEBUG -> "D"
        Log.INFO -> "I"
        Log.WARN -> "W"
        Log.ERROR -> "E"
        Log.ASSERT -> "A"
        else -> "?"
    }
}
