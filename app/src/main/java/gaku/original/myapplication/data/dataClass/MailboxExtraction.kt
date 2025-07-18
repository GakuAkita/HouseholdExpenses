package gaku.original.myapplication.data.dataClass

import gaku.original.myapplication.data.CheckResult
import gaku.original.myapplication.data.Constants.Status.CheckStatus

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

sealed class MailboxExtractionType {
    abstract val enabled: Boolean
    abstract val nodeName: String
    abstract val menuName: String

    abstract val categoryAssignFlag: Int
    abstract fun defaultInstance(): MailboxExtractionType

    data class RakutenPay(
        override val enabled: Boolean = false,
    ) : MailboxExtractionType() {
        override val nodeName = "rakuten_pay"
        override val menuName = "楽天Pay"
        override fun defaultInstance() = RakutenPay()
        override val categoryAssignFlag = CategoryAssignFlag.STORE_NAME
    }

    data class ShikokuElectricPower(
        override val enabled: Boolean = false,
        val categoryId: String? = null,
    ) : MailboxExtractionType() {
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
    ) : MailboxExtractionType() {
        override val nodeName = "amazon_kindle"
        override val menuName = "Amazon Kindle"
        override fun defaultInstance() = AmazonKindle()
        override val categoryAssignFlag = CategoryAssignFlag.NONE
    }

    data class AmazonItem(
        override val enabled: Boolean = false,
    ) : MailboxExtractionType() {
        override val nodeName = "amazon_item"
        override val menuName = "Amazon 物"
        override fun defaultInstance() = AmazonItem()
        override val categoryAssignFlag = CategoryAssignFlag.PRODUCT_NAME
    }
    //ユニクロ？
}

fun getMailboxExtractionTypeByNodeName(
    nodeName: String,
): MailboxExtractionType? {
    return when (nodeName) {
        MailboxExtractionType.RakutenPay().nodeName -> MailboxExtractionType.RakutenPay()
        MailboxExtractionType.ShikokuElectricPower().nodeName -> MailboxExtractionType.ShikokuElectricPower()
        MailboxExtractionType.AmazonKindle().nodeName -> MailboxExtractionType.AmazonKindle()
        MailboxExtractionType.AmazonItem().nodeName -> MailboxExtractionType.AmazonItem()
        //data classを新たに追加したときはここにも増やさないといけない。
        else -> null
    }
}


fun convertNodeNameToMenuName(nodeName: String): String {
    val instance = getMailboxExtractionTypeByNodeName(nodeName)
    return instance?.menuName ?: "不明"
}

fun checkAssignmentInput(assignment: CategoryAssignment): CheckResult {
    return when {
        assignment.name.isNullOrBlank() -> CheckResult(CheckStatus.NG, "店名が入力されていません")
        assignment.condition.isNullOrBlank() -> CheckResult(
            CheckStatus.NG,
            "一致条件が選択されていません"
        )

        assignment.categoryId.isNullOrBlank() -> CheckResult(
            CheckStatus.NG,
            "カテゴリーが選択されていません"
        )

        else -> CheckResult(CheckStatus.OK, "")
    }
}


fun checkAssignmentDuplicate(
    assignment: CategoryAssignment,
    existingAssignments: Map<String, CategoryAssignment>?
): CheckResult {
    if (existingAssignments == null) {
        return CheckResult(
            status = CheckStatus.OK,
            errorMessage = ""
        )
    }

    val isDuplicate = existingAssignments.any { (id, existing) ->
        // 自分自身（更新中）ならスキップ
        if (assignment.id != null && id == assignment.id) return@any false

        existing.name == assignment.name &&
                existing.condition == assignment.condition
    }

    if (isDuplicate) {
        return CheckResult(
            status = CheckStatus.NG,
            errorMessage = "同じ条件の割り当てがすでに存在します"
        )
    }

    return CheckResult(
        status = CheckStatus.OK,
        errorMessage = ""
    )
}

fun checkAssignment(
    assignment: CategoryAssignment,
    existingAssignments: Map<String, CategoryAssignment>?
): CheckResult {
    /* 値が入っているかチェック */
    val inputCheck = checkAssignmentInput(assignment)
    if (inputCheck.status != CheckStatus.OK) {
        return inputCheck
    }

    /* ダブりチェック */
    val duplicateCheck = checkAssignmentDuplicate(
        assignment,
        existingAssignments
    )
    if (duplicateCheck.status != CheckStatus.OK) {
        return duplicateCheck
    }

    return CheckResult(
        status = CheckStatus.OK,
        errorMessage = ""
    )
}