package gaku.original.myapplication.viewModel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Expense
import gaku.original.myapplication.data.ExpenseRepository
import gaku.original.myapplication.fromLocalDateTime
import java.time.LocalDateTime
import javax.inject.Inject

class TemporaryExpenseViewModel @Inject constructor(

): ViewModel(){

    // 初期値として null もしくは適切なデフォルト値を設定
    /*** AddEditとMainViewのデータの受け渡しに使う ***/
    // 内部で状態を管理する
    private val _tmpExpense = mutableStateOf(
        Expense(
            id = null,
            datetime = fromLocalDateTime(LocalDateTime.now()),
            amount = null,
            category = null,
            note = null,
            generatedType = null
        )
    )

    // 外部には読み取り専用のインターフェースを公開
    val tmpExpense: State<Expense> get() = _tmpExpense

    //内部からのみ相対を変更できるようにする。
    //こうすると、UIから直接代入はできないが、TmpViewModelを渡された先のViewModelでは変更できる
    fun updateTmpExpense(newExpense:Expense){
        _tmpExpense.value = newExpense
    }

    // tmpExpenseを一旦リセットする
    fun resetTmpExpense() {
        _tmpExpense.value = Expense(
            id = null,
            datetime = fromLocalDateTime(LocalDateTime.now()),
            amount = null,
            category = null,
            note = null,
            generatedType = null
        )
    }
}