package gaku.original.myapplication.Utility

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
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

    /**
     * 引数のローカル日時を設定のタイムゾーンとして捉えてそれをISOStringに変換する
     */
    fun localDateTimeToIsoString(localDateTime: LocalDateTime?): String? {
        return localDateTime?.atZone(zoneId)  // ローカル日時をユーザータイムゾーンとみなす
            ?.toInstant()                     // UTCに変換
            ?.toString()                      // ISO 8601（Z付き）文字列へ
    }

    /* タイムゾーンの現在時刻を取得。 */
    fun getCurrentTimeInZone(): LocalDateTime {
        return LocalDateTime.now(zoneId)
    }

    /* UTCの時間をLocalDateTimeで取得 */
    fun getCurrentTimeInUTC(): LocalDateTime {
        return LocalDateTime.now(ZoneId.of("UTC"))
    }

    fun getCurrentTimeInUTCString(): String? {
        // UTCの現在時刻をISO 8601形式の文字列で返す
        return getCurrentTimeInUTC().toInstant(ZoneOffset.UTC).toString()
    }

    fun fromInstantUTC(instant: Instant?): String? {
        return instant?.atZone(zoneId)?.toInstant()?.toString()
    }
}