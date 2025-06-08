package gaku.original.myapplication.data.dataClass

import kotlin.reflect.KClass

interface MailAutoExtractionCommon {
    val enabled: Boolean
    val documentName: String
    val menuName: String
}

data class MailAutoExtraction(
    val enabled: Boolean = true//ここをfalseにしたら全部止めるみたいな仕様にするか。
) {
    /**
     * 内部に各メールフォーマットに対して
     * 定義していく
     */
    data class RakutenPay(
        override val enabled: Boolean = false,
        val shopCategoryAssignments: Map<String, String>? = null/* {"shop名" : "categoryID"}として保存 */
    ) : MailAutoExtractionCommon {
        override val documentName = "rakuten_pay"
        override val menuName = "楽天Pay"
    }

    data class ShikokuElectricPower(
        override val enabled: Boolean = false,
        val categoryId: String? = null,
    ) : MailAutoExtractionCommon {
        override val documentName = "shikoku_electric_power"
        override val menuName = "四国電力"
    }

    data class AmazonKindle(
        override val enabled: Boolean = false,
        val categoryId: String? = null,
    ) : MailAutoExtractionCommon {
        override val documentName = "amazon_kindle"
        override val menuName = "Amazon Kindle"
    }

    data class AmazonItem(
        override val enabled: Boolean = false,
        val itemCategoryAssignments: Map<String, String>? = null,
    ) : MailAutoExtractionCommon {
        override val documentName = "amazon_item"
        override val menuName = "Amazon　物"
    }
}

/**
 * MailAutoExtractionの内部クラスであれば変換
 * そうでなければnullを返す
 */
fun getMailAutoExtractionInternalClass(
    instance: MailAutoExtractionCommon
): KClass<out MailAutoExtractionCommon>? {
    return MailAutoExtraction::class.nestedClasses
        .filterIsInstance<KClass<out MailAutoExtractionCommon>>()
        .firstOrNull { it.isInstance(instance) }
}

