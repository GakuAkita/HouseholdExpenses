package gaku.original.myapplication.data.Interface

interface CategoryAssignable {
    val categoryAssignFlag: Int
}

object CategoryAssignFlag {
    const val NONE = 0                // 0000
    const val PRODUCT_NAME = 0x0001  // 0001
    const val STORE_NAME = 0x0002   // 0010
}

/**
 * interfaceには実態がないので、
 * 実態が欲しい場合はこれを使う
 * これだと複数持つときがむりか、、まあいいか。しばらくそういうケースはないし。
 */
enum class CategoryAssignPattern(override val categoryAssignFlag: Int) : CategoryAssignable {
    NONE(CategoryAssignFlag.NONE),
    PRODUCT(CategoryAssignFlag.PRODUCT_NAME),
    STORE(CategoryAssignFlag.STORE_NAME)
}

fun CategoryAssignable.hasProductName(): Boolean {
    return categoryAssignFlag and CategoryAssignFlag.PRODUCT_NAME != 0
}

fun CategoryAssignable.hasStoreName(): Boolean {
    return categoryAssignFlag and CategoryAssignFlag.STORE_NAME != 0
}

fun CategoryAssignable.isProductName(): Boolean {
    return categoryAssignFlag == CategoryAssignFlag.PRODUCT_NAME
}

fun CategoryAssignable.isStoreName(): Boolean {
    return categoryAssignFlag == CategoryAssignFlag.STORE_NAME
}

fun CategoryAssignable.isNone(): Boolean {
    return categoryAssignFlag == CategoryAssignFlag.NONE
}