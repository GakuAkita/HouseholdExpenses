package gaku.original.myapplication.data.dataClass

import gaku.original.myapplication.data.Interface.CategorizationMode
import gaku.original.myapplication.data.Interface.CategoryAssignFlag
import gaku.original.myapplication.data.Interface.HasCategoryId

/**
 * Bitフラグで店名で指定なのか、製品名で指定なのか
 * これ増えてくることはるか？
 * 今のところBitを使うことはなさそうだが、、
 */

/**
 * Firestoreには"gamil"ではなくて、"GMAIL"で保存されるらしい、、
 */
enum class EmailProvider(val value: String) {
    GMAIL("gmail"),
    YAHOO("yahoo"),//非対応
    OUTLOOK("outlook");//非対応
}

sealed class EmailTemplateType : CategorizationMode {
    abstract val enabled: Boolean
    abstract val nodeName: String
    abstract val menuName: String
    abstract val emailProvider: EmailProvider

    abstract override val categoryAssignFlag: Int
    abstract fun defaultInstance(): EmailTemplateType

    data class RakutenPay(
        override val enabled: Boolean = false,
        override val emailProvider: EmailProvider = EmailProvider.GMAIL,
    ) : EmailTemplateType() {
        override val nodeName = "rakuten_pay"
        override val menuName = "楽天Pay"
        override fun defaultInstance() = RakutenPay()
        override val categoryAssignFlag = CategoryAssignFlag.STORE_NAME.value

    }

    data class ShikokuElectricPower(
        override val enabled: Boolean = false,
        override val emailProvider: EmailProvider = EmailProvider.GMAIL,
        override val categoryId: String? = null,
    ) : EmailTemplateType(), HasCategoryId {
        override val nodeName = "shikoku_electric_power"
        override val menuName = "四国電力"
        override fun defaultInstance() = ShikokuElectricPower()
        override val categoryAssignFlag = CategoryAssignFlag.NONE.value
    }

    /**
     * 例えば漫画とかは商品ごとにカテゴリー割り当てたいとかあるかなあ？？
     */
    data class AmazonKindle(
        override val enabled: Boolean = false,
        override val emailProvider: EmailProvider = EmailProvider.GMAIL,
        override val categoryId: String? = null,
    ) : EmailTemplateType(), HasCategoryId {
        override val nodeName = "amazon_kindle"
        override val menuName = "Amazon Kindle"
        override fun defaultInstance() = AmazonKindle()
        override val categoryAssignFlag = CategoryAssignFlag.NONE.value
    }

    data class AmazonItem(
        override val enabled: Boolean = false,
        override val emailProvider: EmailProvider = EmailProvider.GMAIL,
    ) : EmailTemplateType() {
        override val nodeName = "amazon_item"
        override val menuName = "Amazon 物"
        override fun defaultInstance() = AmazonItem()
        override val categoryAssignFlag = CategoryAssignFlag.PRODUCT_NAME.value
    }
    //ユニクロ？
}

/* EmailTemplateTypeでもcopy()てきなのが使えるように。 */
fun EmailTemplateType.copyWith(
    enabled: Boolean = this.enabled,
    categoryId: String? = (this as? HasCategoryId)?.categoryId,
    emailProvider: EmailProvider = this.emailProvider,
): EmailTemplateType =
    when (this) {
        is EmailTemplateType.RakutenPay -> this.copy(
            enabled = enabled,
            emailProvider = emailProvider
        )

        is EmailTemplateType.AmazonItem -> this.copy(
            enabled = enabled,
            emailProvider = emailProvider
        )

        is EmailTemplateType.AmazonKindle -> this.copy(
            enabled = enabled,
            emailProvider = emailProvider,
            categoryId = categoryId
        )

        is EmailTemplateType.ShikokuElectricPower -> this.copy(
            enabled = enabled,
            emailProvider = emailProvider,
            categoryId = categoryId
        )
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

data class MailboxExtractionLastExec(
    val lastMsgId: String? = null,
    val timestamp: Long = 0,
)