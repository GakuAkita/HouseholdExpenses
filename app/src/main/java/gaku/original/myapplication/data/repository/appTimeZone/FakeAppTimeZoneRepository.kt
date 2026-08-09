package gaku.original.myapplication.data.repository.appTimeZone

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.time.ZoneId

class FakeAppTimeZoneRepository: AppTimeZoneRepository{
    val _zoneId = MutableStateFlow(ZoneId.of("Asia/Tokyo"))
    override val zoneId: StateFlow<ZoneId>
        get() = _zoneId

    override fun startListening() {
        _zoneId.value = ZoneId.of("Asia/Tokyo")
    }

    override fun stopListening() {

    }

    override suspend fun getZoneId(newZoneId: ZoneId): ZoneId {
        return _zoneId.value
    }

    override suspend fun updateZoneId(newZoneId: ZoneId) {
        _zoneId.value = newZoneId
    }
}