package gaku.original.myapplication.data.extractor

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap
import gaku.original.myapplication.common.AppResult
import kotlinx.coroutines.delay

class FakeExtractor : Extractor {
    override suspend fun extract(image: Uri): AppResult<ExtractedData, ExtractorError> {
        delay(2000)

        val bitmap = createBitmap(1000, 300).apply {
            eraseColor(Color.White.toArgb())
        }
        return AppResult.Failure(
            ExtractorError.MaskNotSetError(
                bitmap = bitmap
            )
        )
//        return AppResult.Success(
//            ExtractedData(
//                sentData = SentData.Expense(
//                    datetime = LocalDateTime.now().toIsoUtcString(ZoneId.systemDefault()),
//                    amount = 1000,
//                    storeName = null
//                ),
//                bitmap = null
//            )
//        )
    }
}