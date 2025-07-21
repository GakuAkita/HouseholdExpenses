package gaku.original.myapplication.data.Interface

/**
 * EmailTemplateTypeには、製品や店名別にカテゴリー割当があるやつと、
 * EmailTemplate一つに対してカテゴリー一個の割当があるやつがある。
 * これ名前が良くないかな、、
 */
interface CategorizationMode {
    val categoryAssignFlag: Int /* 将来的にstoreとproduct両方持つやつとか出てきそう。 */
}

object CategoryAssignFlag {
    data class Flag(val value: Int, val label: String)

    val NONE = Flag(0, "割り当てなし")
    val PRODUCT_NAME = Flag(0x0001, "商品名")
    val STORE_NAME = Flag(0x0002, "店舗名")

    val allFlags = listOf(NONE, PRODUCT_NAME, STORE_NAME)

    fun fromValue(value: Int): List<Flag> =
        allFlags.filter { value and it.value != 0 }
}


/**
 * interfaceには実態がないので、
 * 実態が欲しい場合はこれを使う
 * これだと複数持つときがむりか、、まあいいか。しばらくそういうケースはないし。
 */
enum class CategoryAssignNamePattern(
    override val categoryAssignFlag: Int,
    val label: String
) : CategorizationMode {
    NONE(CategoryAssignFlag.NONE.value, CategoryAssignFlag.NONE.label),
    PRODUCT(CategoryAssignFlag.PRODUCT_NAME.value, CategoryAssignFlag.PRODUCT_NAME.label),
    STORE(CategoryAssignFlag.STORE_NAME.value, CategoryAssignFlag.STORE_NAME.label);

//    companion object {
//        /* valueからlabelを取得したいとき */
//        fun labelFromValue(value: Int): String? {
//            return entries.firstOrNull { it.categoryAssignFlag == value }?.label
//        }
//    }
}

fun CategorizationMode.hasProductName(): Boolean {
    return categoryAssignFlag and CategoryAssignFlag.PRODUCT_NAME.value != 0
}

fun CategorizationMode.hasStoreName(): Boolean {
    return categoryAssignFlag and CategoryAssignFlag.STORE_NAME.value != 0
}

fun CategorizationMode.isProductName(): Boolean {
    return categoryAssignFlag == CategoryAssignFlag.PRODUCT_NAME.value
}

fun CategorizationMode.isStoreName(): Boolean {
    return categoryAssignFlag == CategoryAssignFlag.STORE_NAME.value
}

fun CategorizationMode.isNone(): Boolean {
    return categoryAssignFlag == CategoryAssignFlag.NONE.value
}