package gaku.original.myapplication.data.extractor

import android.graphics.Bitmap
import gaku.original.myapplication.common.AppError
import gaku.original.myapplication.common.AppResult
import gaku.original.myapplication.ui.screens.receiver.shareReceiver.SentData
import java.io.File

interface Extractor {
    suspend fun extract(image: File): AppResult<ExtractedData, ExtractorError>
}

sealed interface ExtractorError : AppError {
    data class MaskNotSetError(val bitmap: Bitmap) : ExtractorError {
        override val message: String
            get() = "Mask setting is not set."
    }

    data object NoStringFoundError : ExtractorError {
        override val message: String
            get() = "No string found."
    }
}

data class ExtractedData(
    val sentData: SentData,
    val bitmap: Bitmap?
)