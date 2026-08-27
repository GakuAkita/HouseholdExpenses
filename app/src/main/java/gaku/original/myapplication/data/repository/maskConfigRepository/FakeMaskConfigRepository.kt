package gaku.original.myapplication.data.repository.maskConfigRepository

class FakeMaskConfigRepository : MaskConfigRepository {

    override suspend fun getMaskConfig(): MaskConfig {
        return MaskConfig.Percent(
            widthPercent = 0.0,
            heightPercent = 0.0,
            topPercent = 0.22,
            leftPercent = 0.22
        )
    }
}