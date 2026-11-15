package gaku.original.myapplication.logging

import java.io.File

/**
 * Appends lines to `app-0.txt` in [dir]. When that file would grow past [maxFileBytes],
 * it is renamed to `app-1.txt` (older ones shift up) and a new `app-0.txt` is started.
 * At most [maxFiles] files are kept; the oldest is deleted.
 *
 * Not thread-safe: callers must write from a single thread.
 */
class RotatingLogFile(
    private val dir: File,
    private val maxFileBytes: Long = 512 * 1024,
    private val maxFiles: Int = 3,
) {
    init {
        require(maxFiles >= 1)
    }

    fun append(line: String) {
        dir.mkdirs()
        val bytes = (line + "\n").toByteArray(Charsets.UTF_8)
        val current = fileAt(0)
        if (current.exists() && current.length() > 0 && current.length() + bytes.size > maxFileBytes) {
            rotate()
        }
        fileAt(0).appendBytes(bytes)
    }

    /** Log files, oldest first. */
    fun files(): List<File> =
        (maxFiles - 1 downTo 0).map(::fileAt).filter { it.exists() }

    fun clear() {
        dir.listFiles()?.forEach { it.delete() }
    }

    private fun rotate() {
        fileAt(maxFiles - 1).delete()
        for (i in maxFiles - 2 downTo 0) {
            val from = fileAt(i)
            if (from.exists()) from.renameTo(fileAt(i + 1))
        }
    }

    private fun fileAt(index: Int) = File(dir, "app-$index.txt")
}
