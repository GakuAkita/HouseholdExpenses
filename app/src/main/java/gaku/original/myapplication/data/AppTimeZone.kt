package gaku.original.myapplication.data

import gaku.original.myapplication.data.Constants.TimeZoneOption
import gaku.original.myapplication.utility.LogAkitaDebug
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeParseException

object AppTimeZone {
    private val _zoneIdFlow = MutableStateFlow(ZoneId.of("Asia/Tokyo"))
    val zoneIdFlow: StateFlow<ZoneId> = _zoneIdFlow.asStateFlow()

    var currentZoneId: ZoneId
        get() = _zoneIdFlow.value
        private set(value) {
            _zoneIdFlow.value = value
        }

    fun updateZoneId(newZoneId: ZoneId) {
        currentZoneId = newZoneId
        // 必要に応じて他の処理を追加
    }

    fun updateStrZoneId(newZoneId: String) {
        try {
            val zoneId = ZoneId.of(newZoneId)
            updateZoneId(zoneId)
        } catch (e: DateTimeParseException) {
            // 無効なタイムゾーンIDの場合の処理
            LogAkitaDebug("Invalid ZoneId: $newZoneId")
        }
    }

    /**
     * ISO文字列で表して扱っているデータはUTC
     */
    fun isoStringToLocalDateTime(isoString: String?): LocalDateTime? {
        return try {
            val instant = Instant.parse(isoString)
            LocalDateTime.ofInstant(instant, currentZoneId)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    /**
     * 引数のローカル日時を設定のタイムゾーンとして捉えてそれをISOStringに変換する
     */
    fun localDateTimeToIsoString(localDateTime: LocalDateTime?): String? {
        return localDateTime?.atZone(currentZoneId)  // ローカル日時をユーザータイムゾーンとみなす
            ?.toInstant()                     // UTCに変換
            ?.toString()                      // ISO 8601（Z付き）文字列へ
    }

    /* タイムゾーンの現在時刻を取得。 */
    fun getCurrentTimeInZone(): LocalDateTime {
        return LocalDateTime.now(currentZoneId)
    }

    /* UTCの時間をLocalDateTimeで取得 */
    fun getCurrentTimeInUTC(): LocalDateTime {
        return LocalDateTime.now(ZoneId.of(TimeZoneOption.UTC.id))
    }

    fun getCurrentTimeInUTCString(): String {
        // UTCの現在時刻をISO 8601形式の文字列で返す
        return getCurrentTimeInUTC().toInstant(ZoneOffset.UTC).toString()
    }

    /* UTCのYearMonth。カレンダーに使う */
    fun getCurrentUtcYearMonth(): YearMonth {
        return YearMonth.from(LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC))
    }


    fun fromInstantUTCToTimezoneIsoStr(instant: Instant?): String? {
        return instant?.atZone(currentZoneId)?.toString()
    }

    /* UNIXのタイムスタンプをUTC instantに変換する */
    fun convertUnixTimestampToInstant(unixTimestamp: Long): Instant {
        val funcName = AppTimeZone::convertUnixTimestampToCurrentTimeInZoneIsoStr.name

        //instantで持てばUTCだろうがcurrentZoneIdだろうがいつでも変換できる
        val instant = Instant.ofEpochMilli(unixTimestamp)
        return instant
    }

    fun convertUnixTimestampToCurrentTimeInZoneIsoStr(unixTimestamp: Long): String? {
        val funcName = AppTimeZone::convertUnixTimestampToCurrentTimeInZoneIsoStr.name
        val unixInstant = convertUnixTimestampToInstant(unixTimestamp)
        return fromInstantUTCToTimezoneIsoStr(unixInstant)
    }
}