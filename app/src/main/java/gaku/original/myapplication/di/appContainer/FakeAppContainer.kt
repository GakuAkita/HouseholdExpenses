package gaku.original.myapplication.di.appContainer

import android.content.Context
import gaku.original.myapplication.data.extraction.extractor.PayPayReceiptExtractor
import gaku.original.myapplication.data.repository.auth.AuthRepository
import gaku.original.myapplication.data.repository.auth.FakeAuthRepository
import gaku.original.myapplication.data.repository.paypayReceipt.FakePayPayReceiptConfigRepository
import gaku.original.myapplication.di.sessionContainer.FakeSessionContainer
import gaku.original.myapplication.di.sessionContainer.SessionContainer
import gaku.original.myapplication.service.ocr.MlkitOcrService
import gaku.original.myapplication.service.ocr.OcrService

open class FakeAppContainer(
    context: Context,
    override val authRepository: AuthRepository = FakeAuthRepository(),
    override val ocrService: OcrService = MlkitOcrService()
) : AppContainer(
    context = context
) {

    override fun createSessionContainer(): SessionContainer {
        val payPayReceiptConfigRepository = FakePayPayReceiptConfigRepository()

        return FakeSessionContainer(
            payPayReceiptConfigRepository = payPayReceiptConfigRepository,
            payPayReceiptExtractor = PayPayReceiptExtractor(
                context = context,
                paypayReceiptConfigRepository = payPayReceiptConfigRepository,
                ocrService = ocrService
            )
        )
    }
}