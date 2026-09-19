package gaku.original.myapplication.data.dataClass

import gaku.original.myapplication.data.Interface.HasId

/**
 * これ正規表現とか将来的には使えないかな？
 */
sealed interface CategoryAssignment : HasId {
    data class Store(
        override var id: String? = null,
        val categoryId: String? = null,
        val name: String? = null, /* 店の名前や商品名 */
        val condition: MatchCondition = MatchCondition.EXACT, /* 完全一致なのか部分一致なのか */
        val regex: Boolean = false,
    ) : CategoryAssignment

    data class Product(
        override var id: String? = null,
        val categoryId: String? = null,
        val name: String? = null,
        val condition: MatchCondition = MatchCondition.EXACT,
        val regex: Boolean = false
    ) : CategoryAssignment
}

enum class MatchCondition {
    EXACT,
    CONTAINS
}