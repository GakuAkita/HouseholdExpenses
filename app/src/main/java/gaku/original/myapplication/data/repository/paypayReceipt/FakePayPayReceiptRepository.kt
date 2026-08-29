package gaku.original.myapplication.data.repository.paypayReceipt

import kotlinx.coroutines.delay

class FakePayPayReceiptConfigRepository : PayPayReceiptConfigRepository {

    var setting: PayPayReceiptOCRSetting = PayPayReceiptOCRSetting(null, null)

    override suspend fun getOCRSetting(): PayPayReceiptOCRSetting {
        delay(1000)
        return setting
    }

    override suspend fun saveOCRSetting(setting: PayPayReceiptOCRSetting) {
        delay(1000)
        this.setting = setting
    }
}