package gaku.original.myapplication.di.appContainer

import gaku.original.myapplication.data.repository.auth.AuthRepository
import gaku.original.myapplication.data.repository.auth.FakeAuthRepository
import gaku.original.myapplication.di.sessionContainer.FakeSessionContainer
import gaku.original.myapplication.di.sessionContainer.SessionContainer
import gaku.original.myapplication.service.ocr.MlkitOcrService
import gaku.original.myapplication.service.ocr.OcrService

open class FakeAppContainer(
    override val authRepository: AuthRepository = FakeAuthRepository(),
    override val ocrService: OcrService = MlkitOcrService()
) : AppContainer() {

    override fun createSessionContainer(): SessionContainer = FakeSessionContainer()
}