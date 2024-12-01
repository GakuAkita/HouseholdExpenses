package gaku.original.myapplication.data.data_interfaces

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface datetimeConverters {
    val formatter: DateTimeFormatter
        get() = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(formatter)
    }

    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let { LocalDateTime.parse(it, formatter) }
    }
}
