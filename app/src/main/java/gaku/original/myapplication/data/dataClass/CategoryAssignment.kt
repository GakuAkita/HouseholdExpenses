package gaku.original.myapplication.data.dataClass

import gaku.original.myapplication.data.Interface.HasId

/**
 * これ正規表現とか将来的には使えないかな？
 */
data class CategoryAssignmentData(
    val storeName: Map<String, CategoryAssignment>? = null,
    val productName: Map<String, CategoryAssignment>? = null,
)

data class CategoryAssignment(
    override var id: String? = null,
    val categoryId: String? = null,
    val name: String? = null, /* 店の名前や商品名 */
    val condition: String? = null, /* 完全一致なのか部分一致なのか */
    val regex: Boolean = false,

    /* 最悪これさえあれば、あとで分類もできるか、、 */
    val generatedType: String? = null/* これでAmazonKindleなのかAmazonItemなのかそれ以外なのかで区別する？ */
) : HasId