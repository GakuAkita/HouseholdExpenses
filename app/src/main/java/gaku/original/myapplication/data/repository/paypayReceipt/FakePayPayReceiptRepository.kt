package gaku.original.myapplication.data.repository.paypayReceipt

import kotlinx.coroutines.delay

class FakePayPayReceiptConfigRepository : PayPayReceiptConfigRepository {

    var setting: PayPayReceiptOCRSetting = PayPayReceiptOCRSetting(
        mask = MaskConfig.Percent(
            widthPercent = 20.0,
            heightPercent = 18.0,
            topPercent = 0.0,
            leftPercent = 0.0
        )
    )

    override suspend fun getOCRSetting(): PayPayReceiptOCRSetting {
        delay(1000)
        return setting
    }

    override suspend fun saveOCRSetting(setting: PayPayReceiptOCRSetting) {
        delay(1000)
        this.setting = setting
    }
}