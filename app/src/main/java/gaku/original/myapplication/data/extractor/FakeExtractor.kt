package gaku.original.myapplication.data.extractor

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap
import gaku.original.myapplication.common.AppResult
import gaku.original.myapplication.data.extractor.paypayReceipt.PayPayReceiptValidator
import gaku.original.myapplication.data.repository.appTimeZone.toIsoUtcString
import gaku.original.myapplication.data.repository.paypayReceipt.MaskConfig
import gaku.original.myapplication.ui.screens.receiver.shareReceiver.SentData
import kotlinx.coroutines.delay
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.time.Duration.Companion.milliseconds

class FakeExtractor : Extractor, PayPayReceiptValidator {
    override suspend fun extract(image: File): AppResult<ExtractedData, ExtractorError> {
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

    override suspend fun validate(
        bitmap: Bitmap,
        maskConfig: MaskConfig.Percent
    ): AppResult<ExtractedData, ExtractorError> {
        delay(500.milliseconds)
        return AppResult.Success(
            ExtractedData(
                sentData = SentData.Expense(
                    datetime = LocalDateTime.now().toIsoUtcString(ZoneId.systemDefault()),
                    amount = 1000,
                    storeName = null
                ),
                bitmap = null
            )
        )
    }
}