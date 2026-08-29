package gaku.original.myapplication.data.repository.paypayReceipt

interface PayPayReceiptConfigRepository {
    suspend fun getOCRSetting(): PayPayReceiptOCRSetting

    suspend fun saveOCRSetting(setting: PayPayReceiptOCRSetting)
}

data class PayPayReceiptOCRSetting(
    val mask: MaskConfig
)

sealed interface MaskConfig {
    data class Percent(
        val widthPercent: Double?,
        val heightPercent: Double?,
        val topPercent: Double?,
        val leftPercent: Double?
    ) : MaskConfig
}