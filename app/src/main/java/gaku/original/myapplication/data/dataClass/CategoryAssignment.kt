package gaku.original.myapplication.data.dataClass

import gaku.original.myapplication.data.Interface.CategoryAssignNamePattern
import gaku.original.myapplication.data.Interface.HasId
import kotlin.reflect.full.memberProperties

/**
 * これ正規表現とか将来的には使えないかな？
 */
data class CategoryAssignmentData(
    val storeName: Map<String, CategoryAssignment>? = null,
    val productName: Map<String, CategoryAssignment>? = null,
)

/**
 * GPTに作ってもらった。DataのすべてのAssignmentを取得
 */
fun CategoryAssignmentData.getAllAssignments(): List<CategoryAssignment> {
    return CategoryAssignmentData::class.memberProperties
        .mapNotNull { prop ->
            val value = prop.get(this)
            if (value is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                value as? Map<String, CategoryAssignment>
            } else null
        }
        .flatMap { it.values }
}

fun CategoryAssignmentData.copyWithUpdatedMap(
    namePattern: CategoryAssignNamePattern,
    updatedMap: Map<String, CategoryAssignment>
): CategoryAssignmentData {
    return when (namePattern) {
        /* 紐づけないとだめ*/
        CategoryAssignNamePattern.STORE -> this.copy(storeName = updatedMap)
        CategoryAssignNamePattern.PRODUCT -> this.copy(productName = updatedMap)
        else -> this
    }
}

fun CategoryAssignmentData.getAssignmentsByNamePattern(namePattern: CategoryAssignNamePattern): Map<String, CategoryAssignment>? {
    return when {
        namePattern == CategoryAssignNamePattern.STORE -> storeName
        namePattern == CategoryAssignNamePattern.PRODUCT -> productName
        else -> null
    }
}

sealed interface CategoryAssignment : HasId {
    data class Store(
        override var id: String? = null,
        val categoryId: String? = null,
        val name: String? = null, /* 店の名前や商品名 */
        val condition: MatchCondition = MatchCondition.EXACT, /* 完全一致なのか部分一致なのか */
        val regex: Boolean = false,
    ) : CategoryAssignment

    data class Product(
        override var id: String? = null,
        val categoryId: String? = null,
        val name: String? = null,
        val condition: MatchCondition = MatchCondition.EXACT,
        val regex: Boolean = false
    ) : CategoryAssignment
}

enum class MatchCondition {
    EXACT,
    CONTAINS
}

//fun checkAssignmentInput(assignment: CategoryAssignment): CheckResult {
//    return when {
//        assignment.name.isNullOrBlank() -> CheckResult(CheckStatus.NG, "名前が入力されていません")
//        assignment.condition.isNullOrBlank() -> CheckResult(
//            CheckStatus.NG,
//            "一致条件が選択されていません"
//        )
//
//        assignment.categoryId.isNullOrBlank() -> CheckResult(
//            CheckStatus.NG,
//            "カテゴリーが選択されていません"
//        )
//
//        else -> CheckResult(CheckStatus.OK, "")
//    }
//}
//
//
//fun checkAssignmentDuplicate(
//    assignment: CategoryAssignment,
//    existingAssignments: Map<String, CategoryAssignment>?
//): CheckResult {
//    if (existingAssignments == null) {
//        return CheckResult(
//            status = CheckStatus.OK,
//            errorMessage = ""
//        )
//    }
//
//    val isDuplicate = existingAssignments.any { (id, existing) ->
//        // 自分自身（更新中）ならスキップ
//        if (assignment.id != null && id == assignment.id) return@any false
//
//        existing.name == assignment.name &&
//                existing.condition == assignment.condition
//    }
//
//    if (isDuplicate) {
//        return CheckResult(
//            status = CheckStatus.NG,
//            errorMessage = "同じ条件の割り当てがすでに存在します"
//        )
//    }
//
//    return CheckResult(
//        status = CheckStatus.OK,
//        errorMessage = ""
//    )
//}
//
//fun checkAssignment(
//    assignment: CategoryAssignment,
//    existingAssignments: Map<String, CategoryAssignment>?
//): CheckResult {
//    /* 値が入っているかチェック */
//    val inputCheck = checkAssignmentInput(assignment)
//    if (inputCheck.status != CheckStatus.OK) {
//        return inputCheck
//    }
//
//    /* ダブりチェック */
//    val duplicateCheck = checkAssignmentDuplicate(
//        assignment,
//        existingAssignments
//    )
//    if (duplicateCheck.status != CheckStatus.OK) {
//        return duplicateCheck
//    }
//
//    return CheckResult(
//        status = CheckStatus.OK,
//        errorMessage = ""
//    )
//}