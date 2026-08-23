package gaku.original.myapplication.data.repository.paypayReceipt

import kotlinx.coroutines.delay

class FakePayPayReceiptRepository: PayPayReceiptRepository {

    override suspend fun getMaskTopRatio(): Float? {
        delay(3000)
        return null
    }

    override suspend fun getMaskLeftRatio(): Float? {
        delay(3000)
        return null
    }
}