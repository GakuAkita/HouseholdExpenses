package gaku.original.myapplication.ui.screens.global.expenseAddEdit

import gaku.original.myapplication.common.InstantSerializer
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.GeneratedType
import kotlinx.serialization.Serializable
import java.time.Instant

/** What the expense add/edit screen is opened for. Passed as a navigation argument. */
@Serializable
sealed interface ExpenseAddEditMode {
    @Serializable
    data class New(val prefill: ExpensePrefill = ExpensePrefill()) : ExpenseAddEditMode

    @Serializable
    data class Edit(val expense: Expense) : ExpenseAddEditMode
}

/** Initial values for a new expense, e.g. the day clicked on the calendar or a shared receipt. */
@Serializable
data class ExpensePrefill(
    @Serializable(with = InstantSerializer::class)
    val datetime: Instant? = null,
    val amount: Long? = null,
    val storeName: String? = null,
    val generatedType: GeneratedType = GeneratedType.Manual,
)
