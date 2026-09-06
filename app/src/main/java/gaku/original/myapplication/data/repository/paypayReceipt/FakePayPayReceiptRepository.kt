package gaku.original.myapplication.data.repository.paypayReceipt

import kotlinx.coroutines.delay

class FakePayPayReceiptConfigRepository : PayPayReceiptConfigRepository {

    var setting: PayPayReceiptOCRSetting = PayPayReceiptOCRSetting(
        mask = MaskConfig.Percent(
            widthPercent = null,
            heightPercent = null,
            topPercent = 0.0,
            leftPercent = 0.0
        )
    )

    override suspend fun getOCRSetting(): PayPayReceiptOCRSetting {
        delay(1000)
        return setting
    }

    override suspend fun saveOCRSetting(setting: PayPayReceiptOCRSetting) {
        delay(5000)
        this.setting = setting
    }
}