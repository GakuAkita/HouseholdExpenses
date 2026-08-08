package gaku.original.myapplication.data.repository.appTimeZone

import kotlinx.coroutines.flow.StateFlow
import java.time.ZoneId

interface AppTimeZoneRepository {
    val zoneId: StateFlow<ZoneId>

    suspend fun updateZoneId(newZoneId: ZoneId)

    suspend fun getZoneId(newZoneId: ZoneId): ZoneId
}