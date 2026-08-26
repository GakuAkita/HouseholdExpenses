package gaku.original.myapplication.data.extraction

import android.net.Uri
import gaku.original.myapplication.data.extraction.extractor.Extractor

class ExtractionFacade(
    private val payPayReceiptExtractor: Extractor
) {
    suspend fun readImage(packageName: String, image: Uri) {
        if (packageName.contains("jp.co.pay.android")) {
            payPayReceiptExtractor.extract(image)
        } else {

        }
    }
}