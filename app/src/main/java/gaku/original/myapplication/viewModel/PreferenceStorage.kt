package gaku.original.myapplication.viewModel

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

object PrefKeys {
    const val EXCLUSION_RATIO_FROM_SCREEN_LEFT_FOR_PAYPAY_RECEIPT_OCR =
        "exclusion_ratio_from_left_screen_for_paypay_receipt_ocr"
}

class PreferenceStorage @Inject constructor(
    @ApplicationContext context: Context
) {
    /* SharedPreferencesにキーが一個あって、そのPreferencesに他のキーがぶら下がる感じ */
    private val prefs: SharedPreferences =
        context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)

    fun putFloat(key: String, value: Float) {
        prefs.edit().putFloat(key, value).apply()
    }

    fun getFloat(key: String, default: Float = 0f): Float = prefs.getFloat(key, default)

    /**
     * IntやStringは必要になったら作る
     */
}