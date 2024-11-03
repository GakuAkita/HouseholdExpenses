package gaku.original.myapplication

import androidx.lifecycle.ViewModel
import gaku.original.myapplication.data.ExpenseClass
import java.time.LocalDateTime

class AddEditViewModel(): ViewModel() {
    var expenseData: ExpenseClass=ExpenseClass(
        id=null,
        datetime= LocalDateTime.now(),
        expense=null,
        category=null,
        note=null
    )

    fun resetExpenseParams(){
        expenseData=ExpenseClass(
            id=null,
            datetime= LocalDateTime.now(),
            expense=null,
            category=null,
            note=null
        )
    }
}