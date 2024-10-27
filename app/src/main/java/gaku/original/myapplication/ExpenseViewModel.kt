package gaku.original.myapplication

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import java.time.LocalDate

/*
rememberの役割
状態の保存：composable関数がrecomposeされる際に状態を維持する。
これにより、ユーザーの操作や外部からのデータ更新によってUIが再描画されたときに、ユーザーが入力した値や選択したオプションがリセットされることがない

mutableStateOfの役割
リアクティブな状態の生成：mutableStateOfは値が変更されると自動的にその値を参照しているUIを再描画する

rememberとViewModelは違う。
 */

class ExpenseViewModel():ViewModel() {
    private val calendarDate = mutableStateOf(LocalDate.now())

    fun getCalendarYear(): Int {
        return calendarDate.value.year
    }

    fun getCalendarMonth():Int{
        return calendarDate.value.monthValue
    }

    fun incrementMonth(){
        calendarDate.value = calendarDate.value.plusMonths(1)
    }

    fun decrementMonth(){
        calendarDate.value = calendarDate.value.minusMonths(1)
    }
}