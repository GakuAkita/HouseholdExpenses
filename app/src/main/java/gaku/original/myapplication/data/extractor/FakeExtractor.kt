package gaku.original.myapplication.data.extractor

import android.net.Uri
import gaku.original.myapplication.common.AppResult
import kotlinx.coroutines.delay

class FakeExtractor : Extractor {
    override suspend fun extract(image: Uri): AppResult<ExtractedData, ExtractorError> {
        delay(2000)
        return AppResult.Failure(
            ExtractorError.MaskNotSetError
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