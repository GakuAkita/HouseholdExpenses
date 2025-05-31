package gaku.original.myapplication.Utility

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeParseException

object AppTimeZone {
    val zoneId: ZoneId get() = ZoneId.of("Asia/Tokyo") // アプリケーションのタイムゾーンを固定する

    fun isoStringToLocalDateTime(isoString: String?): LocalDateTime? {
        return try {
            val instant = Instant.parse(isoString)
            LocalDateTime.ofInstant(instant, zoneId)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    fun localDateTimeToIsoString(localDateTime: LocalDateTime?): String? {
        return localDateTime?.atZone(zoneId)  // ローカル日時をユーザータイムゾーンとみなす
            ?.toInstant()                     // UTCに変換
            ?.toString()                      // ISO 8601（Z付き）文字列へ
    }
}