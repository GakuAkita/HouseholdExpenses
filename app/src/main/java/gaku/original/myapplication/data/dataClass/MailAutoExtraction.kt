package gaku.original.myapplication.data.dataClass

/**
 * 楽天Payのメール抽出
 */

data class MailAutoExtraction(
    val enabled: Boolean = true//ここをfalseにしたら全部止めるみたいな仕様にするか。
) {
    /**
     * 内部に各メールフォーマットに対して
     * 定義していく
     */
    data class RakutenPay(
        val enabled: Boolean = false,
        val shopCategoryAssignments: Map<String, String>?/* {"shop名" : "categoryID"}として保存 */
    ) {
        companion object {
            const val documentName = "rakuten_pay"
        }
    }

    data class ShikokuElectricPower(
        val enabled: Boolean = false,
        val categoryId: String? = null,
    ) {
        companion object {
            const val documentName = "shikoku_electric_power"
        }
    }

    data class AmazonKindle(
        val enabled: Boolean = false,
        val categoryId: String? = null,
    ) {
        companion object {
            const val documentName = "amazon_kindle"
        }
    }

    data class AmazonItem(
        val enabled: Boolean = false,
        val itemCategoryAssignments: Map<String, String>? = null,
    ) {
        companion object {
            const val documentName = "amazon_item"
        }
    }
    
    //内部data classが増えたらenumにも追加する
}

enum class MailAutoExtrInternalType(val documentName: String) {
    RakutenPay(MailAutoExtraction.RakutenPay.documentName),
    ShikokuElectricPower(MailAutoExtraction.ShikokuElectricPower.documentName),
    AmazonKindle(MailAutoExtraction.AmazonKindle.documentName),
    AmazonItem(MailAutoExtraction.AmazonItem.documentName)
    //増えたらここに追加する
}