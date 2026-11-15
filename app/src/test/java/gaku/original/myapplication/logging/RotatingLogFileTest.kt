package gaku.original.myapplication.logging

import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class RotatingLogFileTest {

    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun append_writesLinesToSingleFileUnderLimit() {
        val log = RotatingLogFile(tmp.root, maxFileBytes = 100, maxFiles = 3)

        log.append("one")
        log.append("two")

        assertEquals(listOf("app-0.txt"), log.files().map { it.name })
        assertEquals("one\ntwo\n", log.files().single().readText())
    }

    @Test
    fun append_rotatesAndDropsOldestBeyondMaxFiles() {
        // Each line is 4 bytes with its newline, so each file holds 2 lines.
        val log = RotatingLogFile(tmp.root, maxFileBytes = 8, maxFiles = 2)

        listOf("aaa", "bbb", "ccc", "ddd", "eee").forEach(log::append)

        // Oldest first: "aaa"/"bbb" were dropped when the third file would have been needed.
        assertEquals(listOf("app-1.txt", "app-0.txt"), log.files().map { it.name })
        assertEquals("ccc\nddd\neee\n", log.files().joinToString("") { it.readText() })
    }

    @Test
    fun append_lineLargerThanLimitStillWritten() {
        val log = RotatingLogFile(tmp.root, maxFileBytes = 4, maxFiles = 2)

        log.append("a long line")

        assertEquals("a long line\n", log.files().single().readText())
    }

    @Test
    fun clear_removesAllFiles() {
        val log = RotatingLogFile(tmp.root, maxFileBytes = 8, maxFiles = 2)
        listOf("aaa", "bbb", "ccc").forEach(log::append)

        log.clear()

        assertEquals(emptyList<String>(), log.files().map { it.name })
    }
}
