package gaku.original.myapplication.data.dataClass

import gaku.original.myapplication.data.CheckResult
import gaku.original.myapplication.data.Constants.Status.CheckStatus

/**
 * これ正規表現とか将来的には使えないかな？
 */
data class CategoryAssignment(
    val id: String? = null,
    val categoryId: String? = null,
    val name: String? = null, /* 店の名前や商品名 */
    val condition: String? = null, /* 完全一致なのか部分一致なのか */
    val regex: Boolean = false
)

sealed class MailboxExtractionType {
    abstract val enabled: Boolean
    abstract val nodeName: String
    abstract val menuName: String

    abstract fun defaultInstance(): MailboxExtractionType

    data class RakutenPay(
        override val enabled: Boolean = false,
        val storeCategoryAssignments: Map<String, CategoryAssignment>? = null
    ) : MailboxExtractionType() {
        override val nodeName = "rakuten_pay"
        override val menuName = "楽天Pay"
        override fun defaultInstance() = RakutenPay()
    }

    data class ShikokuElectricPower(
        override val enabled: Boolean = false,
        val categoryId: String? = null,
    ) : MailboxExtractionType() {
        override val nodeName = "shikoku_electric_power"
        override val menuName = "四国電力"
        override fun defaultInstance() = ShikokuElectricPower()
    }

    data class AmazonKindle(
        override val enabled: Boolean = false,
        val categoryId: String? = null,
    ) : MailboxExtractionType() {
        override val nodeName = "amazon_kindle"
        override val menuName = "Amazon Kindle"
        override fun defaultInstance() = AmazonKindle()
    }

    data class AmazonItem(
        override val enabled: Boolean = false,
        val itemCategoryAssignments: Map<String, CategoryAssignment>? = null,
    ) : MailboxExtractionType() {
        override val nodeName = "amazon_item"
        override val menuName = "Amazon 物"
        override fun defaultInstance() = AmazonItem()
    }
}

/**
 * MailboxExtractionの内部クラスであれば変換
 * そうでなければnullを返す
 * sealed classにしたので不要
 */
//fun getMailboxExtractionInternalClass(
//    instance: MailboxExtractionCommon
//): KClass<out MailboxExtractionCommon>? {
//    return MailboxExtraction::class.nestedClasses
//        .filterIsInstance<KClass<out MailboxExtractionCommon>>()
//        .firstOrNull { it.isInstance(instance) }
//}


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