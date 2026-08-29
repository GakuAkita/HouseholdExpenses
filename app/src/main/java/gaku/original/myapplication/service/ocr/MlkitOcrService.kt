package gaku.original.myapplication.service.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import kotlinx.coroutines.tasks.await

class MlkitOcrService : OcrService {

    override suspend fun runOcr(bitmap: Bitmap): OcrResult {
        val image = InputImage.fromBitmap(bitmap, 0)

        val recognizer =
            TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())

        val visionText: Text? = recognizer.process(image).await()

        if (visionText == null ||
            visionText.textBlocks.isEmpty()
        ) {
            return OcrResult(
                lines = listOf()
            )
        }

        // sort by top→bottom（left→right
        /* OCR doesn't necessarily return text data by order */
        val blocks = visionText.textBlocks.sortedWith(
            compareBy(
                { it.boundingBox?.top ?: Int.MAX_VALUE },
                { it.boundingBox?.left ?: Int.MAX_VALUE },
            )
        )

        return OcrResult(
            lines = blocks.map { it.text }
        )
    }
}