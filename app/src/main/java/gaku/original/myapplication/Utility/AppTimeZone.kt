package gaku.original.myapplication.Utility

import java.time.ZoneId

object AppTimeZone {
    val zoneId: ZoneId get() = ZoneId.of("Asia/Tokyo") // アプリケーションのタイムゾーンを固定する
}