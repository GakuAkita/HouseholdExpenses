package gaku.original.myapplication.data.Constants

/**
 * idを取り出したいときは、TimeZoneOption.JAPAN.id のように使う。
 * labelを取り出したいときは、TimeZoneOption.JAPAN.label のように使う。
 */
enum class TimeZoneOption(val id: String, val label: String) {
    JAPAN("Asia/Tokyo", "日本標準時"),
    UTC("UTC", "協定世界時");

    override fun toString() = label
}

