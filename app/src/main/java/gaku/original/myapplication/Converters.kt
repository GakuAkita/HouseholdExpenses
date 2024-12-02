package gaku.original.myapplication

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
    return dateTime?.format(formatter)
}

fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
    return dateTimeString?.let { LocalDateTime.parse(it, formatter) }
}
