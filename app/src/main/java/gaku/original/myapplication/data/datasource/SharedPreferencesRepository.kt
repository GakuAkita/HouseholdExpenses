package gaku.original.myapplication.data.datasource

import android.content.Context
import androidx.core.content.edit

class SharedPreferencesDataSource(
    private val userId: String,
    private val context: Context
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

object PrefKeys {
    const val PAYPAY_RECEIPT_LEFT_MASK_RATIO = "paypay_receipt_left_mask_ratio"
    const val PAYPAY_RECEIPT_TOP_MASK_RATIO = "paypay_receipt_top_mask_ratio"

    // SearchFilter用のキー
    const val SEARCH_FILTER_GENERATED_TYPES = "search_filter_generated_types"
    const val SEARCH_FILTER_CATEGORY_IDS = "search_filter_category_ids"
    const val SEARCH_FILTER_DATE_FROM = "search_filter_date_from"
    const val SEARCH_FILTER_DATE_TO = "search_filter_date_to"
    const val SEARCH_FILTER_AMOUNT_MIN = "search_filter_amount_min"
    const val SEARCH_FILTER_AMOUNT_MAX = "search_filter_amount_max"
    const val SEARCH_FILTER_STORE_NAME = "search_filter_store_name"
    const val SEARCH_FILTER_ITEM_NAME = "search_filter_item_name"
    const val SEARCH_FILTER_NOTE = "search_filter_note"
}