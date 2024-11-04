package gaku.original.myapplication

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

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

    fun dateUpdate(newDate: LocalDate) {

    }

    fun timeUpdate(newTime: LocalTime) {

    }

    //expenseの更新
    fun expenseUpdate(newExpense: String) {
        val numericExpense = newExpense.toIntOrNull()
        if(numericExpense!=null) {
            expense.value = numericExpense
        }
        else{//nullだったら
            expense.value = null
        }
    }

    //categoryの更新

    //noteの更新
    fun noteUpdate(newNote: String) {
        note.value = newNote
    }
}