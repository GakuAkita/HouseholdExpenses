package gaku.original.myapplication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.time.LocalDate

class ExpenseViewModel():ViewModel() {
    var calendarYear by remember { mutableStateOf(LocalDate.now().year) }
    var calendarMonth by remember {mutableStateOf(LocalDate.now().monthValue)}
}