package gaku.original.myapplication.data.repository.paypayReceipt

interface PayPayReceiptRepository {
    suspend fun getMaskTopRatio():Float?
    suspend fun getMaskLeftRatio():Float?
}