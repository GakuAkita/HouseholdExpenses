package gaku.original.myapplication.service.ocr

import android.graphics.Bitmap

interface OcrService {
    suspend fun runOcr(bitmap: Bitmap): OcrResult
}

data class OcrResult(
    val lines: List<String>
)