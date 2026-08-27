package gaku.original.myapplication.data.extraction.extractor

import android.net.Uri
import gaku.original.myapplication.common.AppError

interface Extractor {
    suspend fun extract(image: Uri)
}

sealed interface ExtractorError : AppError