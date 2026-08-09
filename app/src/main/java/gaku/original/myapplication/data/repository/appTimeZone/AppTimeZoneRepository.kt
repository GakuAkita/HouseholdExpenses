package gaku.original.myapplication.data.repository.appTimeZone

import com.google.type.TimeZone
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

interface AppTimeZoneRepository {
    val zoneId: StateFlow<ZoneId>

    fun startListening()

    fun stopListening()

    suspend fun updateZoneId(newZoneId: ZoneId)

    suspend fun getZoneId(newZoneId: ZoneId): ZoneId
}

fun String.toLocalDateTime(zoneId: ZoneId): LocalDateTime {
    val instant = Instant.parse(this)
    return LocalDateTime.ofInstant(instant,zoneId)
}

fun LocalDateTime.toIsoUtcString(zoneId: ZoneId): String{
    return this.atZone(zoneId).toInstant().toString()
}

fun LocalDateTime.toInstant(zoneId: ZoneId): Instant = this.atZone(zoneId).toInstant()