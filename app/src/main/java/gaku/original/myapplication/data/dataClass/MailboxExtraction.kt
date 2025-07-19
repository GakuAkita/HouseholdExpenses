package gaku.original.myapplication.data.dataClass

import gaku.original.myapplication.data.Interface.CategoryAssignable

/**
 * Bitフラグで店名で指定なのか、製品名で指定なのか
 * これ増えてくることはるか？
 * 今のところBitを使うことはなさそうだが、、
 */
object CategoryAssignFlag {
    const val NONE = 0                // 0000
    const val PRODUCT_NAME = 0x0001  // 0001
    const val STORE_NAME = 0x0002   // 0010
}

enum class EmailProvider(val value: String) {
    GMAIL("gmail"),
    YAHOO("yahoo"),//非対応
    OUTLOOK("outlook");//非対応
}

sealed class EmailTemplateType : CategoryAssignable {
    abstract val enabled: Boolean
    abstract val nodeName: String
    abstract val menuName: String

    abstract override val categoryAssignFlag: Int
    abstract fun defaultInstance(): EmailTemplateType

    data class RakutenPay(
        override val enabled: Boolean = false,
    ) : EmailTemplateType() {
        override val nodeName = "rakuten_pay"
        override val menuName = "楽天Pay"
        override fun defaultInstance() = RakutenPay()
        override val categoryAssignFlag = CategoryAssignFlag.STORE_NAME
    }

    data class ShikokuElectricPower(
        override val enabled: Boolean = false,
        val categoryId: String? = null,
    ) : EmailTemplateType() {
        override val nodeName = "shikoku_electric_power"
        override val menuName = "四国電力"
        override fun defaultInstance() = ShikokuElectricPower()
        override val categoryAssignFlag = CategoryAssignFlag.NONE
    }

    /**
     * 例えば漫画とかは商品ごとにカテゴリー割り当てたいとかあるかなあ？？
     */
    data class AmazonKindle(
        override val enabled: Boolean = false,
        val categoryId: String? = null,
    ) : EmailTemplateType() {
        override val nodeName = "amazon_kindle"
        override val menuName = "Amazon Kindle"
        override fun defaultInstance() = AmazonKindle()
        override val categoryAssignFlag = CategoryAssignFlag.NONE
    }

    data class AmazonItem(
        override val enabled: Boolean = false,
    ) : EmailTemplateType() {
        override val nodeName = "amazon_item"
        override val menuName = "Amazon 物"
        override fun defaultInstance() = AmazonItem()
        override val categoryAssignFlag = CategoryAssignFlag.PRODUCT_NAME
    }
    //ユニクロ？
}

fun getEmailTemplateTypeByNodeName(
    nodeName: String,
): EmailTemplateType? {
    return when (nodeName) {
        EmailTemplateType.RakutenPay().nodeName -> EmailTemplateType.RakutenPay()
        EmailTemplateType.ShikokuElectricPower().nodeName -> EmailTemplateType.ShikokuElectricPower()
        EmailTemplateType.AmazonKindle().nodeName -> EmailTemplateType.AmazonKindle()
        EmailTemplateType.AmazonItem().nodeName -> EmailTemplateType.AmazonItem()
        //data classを新たに追加したときはここにも増やさないといけない。
        else -> null
    }
}


fun convertNodeNameToMenuName(nodeName: String): String {
    val instance = getEmailTemplateTypeByNodeName(nodeName)
    return instance?.menuName ?: "不明"
}

