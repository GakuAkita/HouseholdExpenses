package gaku.original.myapplication.di.appContainer

import android.content.Context
import gaku.original.myapplication.data.extractor.FakeExtractor
import gaku.original.myapplication.data.repository.auth.AuthRepository
import gaku.original.myapplication.data.repository.auth.FakeAuthRepository
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

        return FakeSessionContainer(
            payPayReceiptExtractor = FakeExtractor()
        )
    }
}