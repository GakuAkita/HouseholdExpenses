package gaku.original.myapplication.data.Constants

import java.time.ZoneId

/**
 * idを取り出したいときは、TimeZoneOption.JAPAN.id のように使う。
 * labelを取り出したいときは、TimeZoneOption.JAPAN.label のように使う。
 */
enum class TimeZone(val id: String, val label: String) {
    JAPAN("Asia/Tokyo", "Japan"),
    TAIWAN("Asia/Taipei", "Taiwan"),
    EUROPE("Europe/Paris", "Europe"),
    USA("America/New_York", "America East"),
    UTC("UTC", "UTC");

    val zoneId: ZoneId
        get() = ZoneId.of(id)

    companion object {
        fun fromId(id: String): TimeZone? =
            entries.find { it.id == id }
    }

    override fun toString() = label
}