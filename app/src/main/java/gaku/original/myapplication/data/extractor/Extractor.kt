package gaku.original.myapplication.data.extractor

import android.net.Uri
import gaku.original.myapplication.common.AppError
import gaku.original.myapplication.common.AppResult
import gaku.original.myapplication.ui.screens.receiver.SentData

interface Extractor {
    suspend fun extract(image: Uri): AppResult<SentData, ExtractorError>
}

sealed interface ExtractorError : AppError {
    data object MaskNotSetError : ExtractorError {
        override val message: String
            get() = "Mask setting is not set."
    }
}