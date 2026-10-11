package gaku.original.myapplication.data.dataClass

/** GeneratedType without its payload, so that it can be used as a search condition. */
enum class GeneratedTypeKind {
    Manual,
    RepeatAdd,
    MailExtraction,
    Import,
}

fun GeneratedType.kind(): GeneratedTypeKind = when (this) {
    GeneratedType.Manual -> GeneratedTypeKind.Manual
    is GeneratedType.RepeatAdd -> GeneratedTypeKind.RepeatAdd
    is GeneratedType.MailExtraction -> GeneratedTypeKind.MailExtraction
    is GeneratedType.Import -> GeneratedTypeKind.Import
}

/**
 * Conditions for searching expenses. An empty condition is not applied.
 * Conditions are combined with AND, values inside one condition with OR.
 */
data class ExpenseSearchFilter(
    /* Partial match against storeName, itemName and note, ignoring case. */
    val keyword: String = "",
    /* A null element matches expenses without a category. */
    val categoryIds: Set<String?> = emptySet(),
    val generatedTypes: Set<GeneratedTypeKind> = emptySet(),
) {
    fun matches(expense: Expense): Boolean {
        val trimmedKeyword = keyword.trim()
        if (trimmedKeyword.isNotEmpty()) {
            val hit = listOfNotNull(expense.storeName, expense.itemName, expense.note)
                .any { it.contains(trimmedKeyword, ignoreCase = true) }
            if (!hit) return false
        }

        if (categoryIds.isNotEmpty() && expense.category?.id !in categoryIds) return false

        if (generatedTypes.isNotEmpty() && expense.generatedType.kind() !in generatedTypes) return false

        return true
    }

    fun isEmpty(): Boolean =
        keyword.isBlank() && categoryIds.isEmpty() && generatedTypes.isEmpty()

    companion object {
        /* Finding expenses that still need a category is the most common search. */
        val Uncategorized = ExpenseSearchFilter(categoryIds = setOf(null))
    }
}
