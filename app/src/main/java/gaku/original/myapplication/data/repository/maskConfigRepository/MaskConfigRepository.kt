package gaku.original.myapplication.data.repository.maskConfigRepository

interface MaskConfigRepository {
    suspend fun getMaskConfig(): MaskConfig
}

sealed interface MaskConfig {
    data class Percent(
        val widthPercent: Double?,
        val heightPercent: Double?,
        val topPercent: Double?,
        val leftPercent: Double?
    ) : MaskConfig
}
