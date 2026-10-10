package gaku.original.myapplication.data.importer

import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

class CsvReader {

    /**
     * Japanese apps often write CSV in Shift_JIS, so anything that is not valid UTF-8 is read as Shift_JIS.
     * windows-31j is Microsoft's Shift_JIS, which also has characters like ① and ㈱.
     */
    fun decode(bytes: ByteArray): String {
        if (bytes.size >= 3 && bytes.copyOfRange(0, 3).contentEquals(UTF8_BOM)) {
            return String(bytes, 3, bytes.size - 3, Charsets.UTF_8)
        }
        return try {
            Charsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString()
        } catch (e: CharacterCodingException) {
            String(bytes, shiftJis())
        }
    }

    private fun shiftJis(): Charset =
        if (Charset.isSupported("windows-31j")) Charset.forName("windows-31j") else Charset.forName("Shift_JIS")

    /**
     * Splits CSV text into records (RFC 4180): fields may be quoted, and a quoted field may hold
     * commas, line breaks and "" for a quote. Blank lines are dropped.
     */
    fun parse(text: String): List<List<String>> {
        val records = mutableListOf<List<String>>()
        var fields = mutableListOf<String>()
        val field = StringBuilder()
        var inQuotes = false
        var i = 0

        fun endRecord() {
            fields.add(field.toString())
            field.clear()
            if (fields.size > 1 || fields[0].isNotBlank()) records.add(fields)
            fields = mutableListOf()
        }

        while (i < text.length) {
            val c = text[i]
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < text.length && text[i + 1] == '"') {
                        field.append('"')
                        i++
                    } else {
                        inQuotes = false
                    }
                } else {
                    field.append(c)
                }
            } else {
                when (c) {
                    '"' -> inQuotes = true
                    ',' -> {
                        fields.add(field.toString())
                        field.clear()
                    }

                    '\r' -> {
                        if (i + 1 < text.length && text[i + 1] == '\n') i++
                        endRecord()
                    }

                    '\n' -> endRecord()
                    else -> field.append(c)
                }
            }
            i++
        }
        if (field.isNotEmpty() || fields.isNotEmpty()) endRecord()
        return records
    }

    private companion object {
        val UTF8_BOM = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
    }
}
