package gaku.original.myapplication.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import gaku.original.myapplication.data.Expense
import gaku.original.myapplication.fromLocalDateTime
import java.time.LocalDateTime

class TemporaryExpenseViewModel : ViewModel(){

    // 初期値として null もしくは適切なデフォルト値を設定
    /*** AddEditとMainViewのデータの受け渡しに使う ***/
    val tmpExpense = mutableStateOf(
        Expense(
            id = null,
            datetime = fromLocalDateTime(LocalDateTime.now()),
            amount = null,
            category = null,
            note = null,
            generatedType = null
        )
    )
}