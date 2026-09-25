package gaku.original.myapplication.data.repository.paypayReceipt

import gaku.original.myapplication.data.datasource.PrefKey
import gaku.original.myapplication.data.datasource.SharedPreferencesDataSource


class PayPayReceiptRepositorySharedPreferences(
    private val sharedPreferencesDataSource: SharedPreferencesDataSource
) : PayPayReceiptConfigRepository {

    override suspend fun getOCRSetting(): PayPayReceiptOCRSetting {
        val leftPercent =
            sharedPreferencesDataSource.getFloat(PrefKey.PayPayReceiptOCR.Mask.LeftStartPercent.key)
        val topPercent =
            sharedPreferencesDataSource.getFloat(PrefKey.PayPayReceiptOCR.Mask.TopStartPercent.key)
        val widthPercent =
            sharedPreferencesDataSource.getFloat(PrefKey.PayPayReceiptOCR.Mask.WidthPercent.key)
        val heightPercent =
            sharedPreferencesDataSource.getFloat(PrefKey.PayPayReceiptOCR.Mask.HeightPercent.key)

        val config = MaskConfig.Percent(
            widthPercent = widthPercent?.toDouble(),
            heightPercent = heightPercent?.toDouble(),
            topPercent = topPercent?.toDouble(),
            leftPercent = leftPercent?.toDouble()
        )
        return PayPayReceiptOCRSetting(config)
    }

    override suspend fun saveOCRSetting(setting: PayPayReceiptOCRSetting) {
        when (setting.mask) {
            is MaskConfig.Percent -> {
                if (setting.mask.leftPercent == null ||
                    setting.mask.topPercent == null ||
                    setting.mask.widthPercent == null ||
                    setting.mask.heightPercent == null
                ) {
                    throw Exception(
                        "Invalid mask config. Mask is not set." +
                                "left=${setting.mask.leftPercent} topPercent=${setting.mask.topPercent}" +
                                "widthPercent=${setting.mask.widthPercent} heightPercent=${setting.mask.heightPercent}"
                    )
                }

                sharedPreferencesDataSource.setFloat(
                    PrefKey.PayPayReceiptOCR.Mask.LeftStartPercent.key,
                    setting.mask.leftPercent.toFloat()
                )
                sharedPreferencesDataSource.setFloat(
                    PrefKey.PayPayReceiptOCR.Mask.TopStartPercent.key,
                    setting.mask.topPercent.toFloat()
                )
                sharedPreferencesDataSource.setFloat(
                    PrefKey.PayPayReceiptOCR.Mask.WidthPercent.key,
                    setting.mask.widthPercent.toFloat()
                )
                sharedPreferencesDataSource.setFloat(
                    PrefKey.PayPayReceiptOCR.Mask.HeightPercent.key,
                    setting.mask.heightPercent.toFloat()
                )
            }
        }
    }
}
