package gaku.original.myapplication.data.dataClass

import kotlin.reflect.KClass

interface MailboxExtractionCommon {
    val enabled: Boolean
    val documentName: String
    val menuName: String
}

data class CategoryAssignment(
    val id: String? = null,
    val categoryId: String? = null,
    val name: String? = null, /* 店の名前や商品名 */
    val condition: String? = null /* 完全一致なのか部分一致なのか */
)

data class MailboxExtraction(
    val enabled: Boolean = true//ここをfalseにしたら全部止めるみたいな仕様にするか。
) {
    /**
     * 内部に各メールフォーマットに対して
     * 定義していく
     */
    data class RakutenPay(
        override val enabled: Boolean = false,
        val storeCategoryAssignments: Map<String, CategoryAssignment>? = null/* {"shop名" : "categoryID"}として保存 */
    ) : MailboxExtractionCommon {
        override val documentName = "rakuten_pay"
        override val menuName = "楽天Pay"
    }

    data class ShikokuElectricPower(
        override val enabled: Boolean = false,
        val categoryId: String? = null,
    ) : MailboxExtractionCommon {
        override val documentName = "shikoku_electric_power"
        override val menuName = "四国電力"
    }

    data class AmazonKindle(
        override val enabled: Boolean = false,
        val categoryId: String? = null,
    ) : MailboxExtractionCommon {
        override val documentName = "amazon_kindle"
        override val menuName = "Amazon Kindle"
    }

    data class AmazonItem(
        override val enabled: Boolean = false,
        val itemCategoryAssignments: Map<String, CategoryAssignment>? = null,
    ) : MailboxExtractionCommon {
        override val documentName = "amazon_item"
        override val menuName = "Amazon　物"
    }
}

/**
 * MailboxExtractionの内部クラスであれば変換
 * そうでなければnullを返す
 */
fun getMailboxExtractionInternalClass(
    instance: MailboxExtractionCommon
): KClass<out MailboxExtractionCommon>? {
    return MailboxExtraction::class.nestedClasses
        .filterIsInstance<KClass<out MailboxExtractionCommon>>()
        .firstOrNull { it.isInstance(instance) }
}

