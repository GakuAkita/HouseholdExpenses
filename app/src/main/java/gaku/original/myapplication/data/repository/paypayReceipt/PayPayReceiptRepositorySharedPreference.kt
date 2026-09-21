package gaku.original.myapplication.data.repository.paypayReceipt

import gaku.original.myapplication.data.datasource.SharedPreferencesDataSource


class PayPayReceiptRepositorySharedPreferences(
    private val sharedPreferencesDataSource: SharedPreferencesDataSource
) : PayPayReceiptConfigRepository {

    override suspend fun getOCRSetting(): PayPayReceiptOCRSetting {
        TODO("Not yet implemented")
    }

    override suspend fun saveOCRSetting(setting: PayPayReceiptOCRSetting) {
        TODO("Not yet implemented")
    }
}
