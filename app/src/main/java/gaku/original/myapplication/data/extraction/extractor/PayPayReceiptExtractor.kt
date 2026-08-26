package gaku.original.myapplication.data.extraction.extractor

import android.net.Uri
import gaku.original.myapplication.service.ocr.OcrService

class PayPayReceiptExtractor(
    private val ocrService: OcrService
) : Extractor {
    override suspend fun extract(image: Uri) {

    }
}