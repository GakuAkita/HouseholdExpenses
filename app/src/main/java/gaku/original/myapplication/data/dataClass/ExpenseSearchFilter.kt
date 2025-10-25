package gaku.original.myapplication.data.dataClass

/**
 * Expenseの検索フィルター条件を表すデータクラス
 */
data class ExpenseSearchFilter(
    // GeneratedTypeでフィルタリング（nullの場合はフィルターしない）
    val generatedTypes: List<String>? = null,
    
    // Categoryでフィルタリング（nullの場合はフィルターしない）
    // categoryIdのリスト。空のリストの場合はcategoryがnullのものを検索
    val categoryIds: List<String?>? = null,
    
    // 日付範囲でフィルタリング（ISO 8601形式の文字列）
    val dateFrom: String? = null,
    val dateTo: String? = null,
    
    // 金額範囲でフィルタリング
    val amountMin: Long? = null,
    val amountMax: Long? = null,
    
    // ストア名で検索（部分一致）
    val storeName: String? = null,
    
    // アイテム名で検索（部分一致）
    val itemName: String? = null,
    
    // メモで検索（部分一致）
    val note: String? = null,
) {
    /**
     * フィルターが空（すべての条件がnull）かどうかを判定
     */
    fun isEmpty(): Boolean {
        return generatedTypes == null &&
                categoryIds == null &&
                dateFrom == null &&
                dateTo == null &&
                amountMin == null &&
                amountMax == null &&
                storeName == null &&
                itemName == null &&
                note == null
    }
    
    /**
     * アクティブなフィルター条件の数を返す
     */
    fun activeFilterCount(): Int {
        var count = 0
        if (generatedTypes != null) count++
        if (categoryIds != null) count++
        if (dateFrom != null || dateTo != null) count++
        if (amountMin != null || amountMax != null) count++
        if (storeName != null) count++
        if (itemName != null) count++
        if (note != null) count++
        return count
    }
}

/**
 * デフォルトのフィルター（カテゴリーがnullのもの）
 */
fun getDefaultSearchFilter(): ExpenseSearchFilter {
    return ExpenseSearchFilter(
        categoryIds = listOf(null) // categoryがnullのものだけ
    )
}

/**
 * 空のフィルター（すべてのExpenseを取得）
 */
fun getEmptySearchFilter(): ExpenseSearchFilter {
    return ExpenseSearchFilter()
}

