package gaku.original.myapplication.data.extractor

import android.net.Uri
import gaku.original.myapplication.common.AppResult
import gaku.original.myapplication.data.repository.appTimeZone.toIsoUtcString
import gaku.original.myapplication.ui.screens.receiver.SentData
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.ZoneId

class FakeExtractor : Extractor {
    override suspend fun extract(image: Uri): AppResult<SentData, ExtractorError> {
        delay(1000)
        return AppResult.Success(
            SentData.Expense(
                datetime = LocalDateTime.now().toIsoUtcString(ZoneId.systemDefault()),
                amount = 1000,
                storeName = "fake store"
            )
        )
    }
}