package gaku.original.myapplication.data.importer

import org.junit.Assert.assertEquals
import org.junit.Test

class CsvReaderTest {

    private val reader = CsvReader()

    @Test
    fun parse_quotedFieldsWithCommasQuotesAndLineBreaks() {
        val text = "a,\"b,c\",\"say \"\"hi\"\"\"\r\n\"multi\nline\",x,\n"

        assertEquals(
            listOf(
                listOf("a", "b,c", "say \"hi\""),
                listOf("multi\nline", "x", ""),
            ),
            reader.parse(text)
        )
    }

    @Test
    fun parse_dropsBlankLines() {
        assertEquals(listOf(listOf("a"), listOf("b")), reader.parse("a\n\n\r\nb"))
    }

    @Test
    fun decode_utf8WithBom() {
        val bytes = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + "日付,金額".toByteArray(Charsets.UTF_8)
        assertEquals("日付,金額", reader.decode(bytes))
    }

    @Test
    fun decode_utf8WithoutBom() {
        assertEquals("食費", reader.decode("食費".toByteArray(Charsets.UTF_8)))
    }

    @Test
    fun decode_shiftJis() {
        val bytes = "日付,金額,①".toByteArray(charset("windows-31j"))
        assertEquals("日付,金額,①", reader.decode(bytes))
    }
}
