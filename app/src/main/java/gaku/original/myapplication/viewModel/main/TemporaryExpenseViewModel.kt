package gaku.original.myapplication.viewModel.main

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.getDefaultExpense
import javax.inject.Inject

class TemporaryExpenseViewModel @Inject constructor(

) : ViewModel() {

    /** AddEditとMainView間でのデータ受け渡し用 **/
    // 内部状態: 常に1件以上のExpenseを保持
    private val _tmpExpenseList = mutableStateOf(
        listOf(getDefaultExpense())
    )

    // 外部公開用（読み取り専用）
    val tmpExpenseList: State<List<Expense>> get() = _tmpExpenseList

    /** 現在編集中のExpense（とりあえず最初の要素を返す） **/
    val currentTmpExpense: Expense
        get() = _tmpExpenseList.value.first()

    /** Expenseを追加 **/
    fun addTmpExpense(newExpense: Expense = getDefaultExpense()) {
        _tmpExpenseList.value = _tmpExpenseList.value + newExpense
    }

    /** 特定インデックスのExpenseを更新 **/
    fun updateTmpExpenseAt(index: Int, newExpense: Expense) {
        _tmpExpenseList.value = _tmpExpenseList.value.toMutableList().apply {
            if (index in indices) {
                this[index] = newExpense
            }
        }
    }

    /** 現在のExpense（先頭）を更新（従来互換） **/
    fun updateTmpExpense(newExpense: Expense) {
        updateTmpExpenseAt(0, newExpense)
    }

    fun removeTmpExpenseExceptHead() {
        _tmpExpenseList.value = _tmpExpenseList.value.take(1)
    }

    /** 特定インデックスのExpenseを削除 **/
    fun removeTmpExpenseAt(index: Int) {
        if (_tmpExpenseList.value.size > 1) { // 最低1件は残す
            _tmpExpenseList.value = _tmpExpenseList.value.toMutableList().apply {
                if (index in indices) {
                    removeAt(index)
                }
            }
        }
    }

    /** 全てリセット（1件のデフォルト状態に戻す） **/
    fun resetTmpExpenseList() {
        _tmpExpenseList.value = listOf(getDefaultExpense())
    }
}