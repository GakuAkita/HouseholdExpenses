package gaku.original.myapplication.data.repository.paypayReceipt

interface PayPayReceiptRepository {
    suspend fun getOCRSetting(): PayPayReceiptOCRSetting

    suspend fun saveOCRSetting(setting: PayPayReceiptOCRSetting)
}

data class PayPayReceiptOCRSetting(
    val topRatio: Float?,
    val leftRatio: Float?
)