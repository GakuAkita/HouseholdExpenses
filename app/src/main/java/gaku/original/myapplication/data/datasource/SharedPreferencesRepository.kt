package gaku.original.myapplication.data.datasource

import android.content.Context
import androidx.core.content.edit

class SharedPreferencesDataSource(
    private val userId: String, private val context: Context
) {
    private val prefs = context.getSharedPreferences("user_prefs_$userId", Context.MODE_PRIVATE)

    // ----- 共通 -----
    fun hasKey(key: String): Boolean = prefs.contains(key)
    fun remove(key: String) = prefs.edit { remove(key) }

    // ----- String -----
    fun getString(key: String, default: String? = null): String? = prefs.getString(key, default)
    fun setString(key: String, value: String) = prefs.edit { putString(key, value) }

    // ----- Int -----
    fun getInt(key: String, default: Int = 0): Int = prefs.getInt(key, default)
    fun setInt(key: String, value: Int) = prefs.edit { putInt(key, value) }

    // ----- Float -----
    fun getFloat(key: String, default: Float = 0f): Float = prefs.getFloat(key, default)
    fun setFloat(key: String, value: Float) = prefs.edit { putFloat(key, value) }

    // ----- Boolean -----
    fun getBoolean(key: String, default: Boolean = false): Boolean = prefs.getBoolean(key, default)
    fun setBoolean(key: String, value: Boolean) = prefs.edit { putBoolean(key, value) }
}

sealed class PrefKey(
    private val prefix: String, private val name: String
) {
    val key: String
        get() = "${prefix}_$name"

    sealed class PayPayReceiptOCR(name: String) : PrefKey("paypay_receipt_ocr", name) {

        sealed class Mask(name: String) : PayPayReceiptOCR("mask_$name") {
            data object LeftStartPercent : Mask("left_start_percent")

            data object TopStartPercent : Mask("top_start_percent")

            object WidthPercent : Mask("width_percent")

            data object HeightPercent : Mask("height_percent")
        }
    }

    sealed class SearchFilter(name: String) : PrefKey("search_filter", name) {

        data object GeneratedTypes : SearchFilter("generated_types")

        data object CategoryIds : SearchFilter("category_ids")

        data object DateFrom : SearchFilter("date_from")

        data object DateTo : SearchFilter("date_to")

        data object AmountMin : SearchFilter("amount_min")

        data object AmountMax : SearchFilter("amount_max")

        data object StoreName : SearchFilter("store_name")

        data object ItemName : SearchFilter("item_name")

        data object Note : SearchFilter("note")
    }
}