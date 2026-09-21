package gaku.original.myapplication.data.repository.paypayReceipt

interface PayPayReceiptConfigRepository {
    suspend fun getOCRSetting(): PayPayReceiptOCRSetting

    suspend fun saveOCRSetting(setting: PayPayReceiptOCRSetting)
}

data class PayPayReceiptOCRSetting(
    val mask: MaskConfig
)

sealed interface MaskConfig {
    /* if the property is null, it means not saved. */
    data class Percent(
        val widthPercent: Double? = null,
        val heightPercent: Double? = null,
        val topPercent: Double? = null,
        val leftPercent: Double? = null
    ) : MaskConfig
}