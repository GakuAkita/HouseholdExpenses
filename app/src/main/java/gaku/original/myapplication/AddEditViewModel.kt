package gaku.original.myapplication

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import java.time.LocalDateTime

class AddEditViewModel(): ViewModel() {
    val datetime = mutableStateOf(LocalDateTime.now())
    val expense = mutableStateOf<Int?>(null)
    val category = mutableStateOf<String?>(null)
    val note = mutableStateOf<String?>(null)

    //初期化
    fun resetExpenseParams(){
        datetime.value=LocalDateTime.now()
        expense.value=null
        category.value=null
        note.value=null
    }

    fun expenseUpdate(newExpense: String) {
        val numericExpense = newExpense.toIntOrNull()
        if(numericExpense!=null) {
            expense.value = numericExpense
        }
        else{//nullだったら
            expense.value = null
        }
    }

}