package gaku.original.myapplication.data.dataClass

import gaku.original.myapplication.data.CheckResult
import gaku.original.myapplication.data.Constants.Status.CheckStatus
import gaku.original.myapplication.data.Interface.CategoryAssignFlag
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

object AssignmentCondition {
    const val CONTAINS = "contains"
    const val EXACT_MATCH = "exact_match"
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

fun CategoryAssignmentData.getAssignmentsByFlag(flag: Int): Map<String, CategoryAssignment>? {
    return when {
        flag and CategoryAssignFlag.STORE_NAME.value != 0 -> storeName
        flag and CategoryAssignFlag.PRODUCT_NAME.value != 0 -> productName
        else -> null
    }
}