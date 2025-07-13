package gaku.original.myapplication.ui.view.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import gaku.original.myapplication.Screen
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.convertGeneratedTypeToDisplayName
import gaku.original.myapplication.ui.common.BottomBarView
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.utility.AppTimeZone
import gaku.original.myapplication.utility.LogAkitaDebug
import gaku.original.myapplication.viewModel.main.NotCategorizedViewModel
import java.time.LocalDateTime

@Composable
fun NotCategorizedView(
    viewModel: NotCategorizedViewModel = hiltViewModel(),
    navController: NavController
) {
    val expenses = viewModel.notCategorizedExpenses.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.fetchNotCategorizedExpenses {
            if (it.status == SuspendFuncStatus.SUCCESS) {
                LogAkitaDebug("fetch success!!")
            } else if (it.status == SuspendFuncStatus.TIMEOUT) {
                LogAkitaDebug("Timeout")
            } else {
                LogAkitaDebug("Failed")
            }
        }
    }

    Scaffold(
        topBar = {
            TopBarView("カテゴリー未割り当て")
        },
        bottomBar = { BottomBarView(navController) }
    ) { innerPadding ->
        /**
         * 新たに抽出したら通知したいな
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth(),
                    userScrollEnabled = true
                ) {
                    items(expenses.value) { expense ->
                        NotCategorizedExpenseItem(expense) {
                            viewModel.setToTmpExpense(it)
                            navController.navigate(
                                Screen.GlobalScreen.ExpenseAddEdit.createRoute(
                                    Screen.NotCategorizedScreen//遷移元
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotCategorizedExpenseItem(
    expense: Expense,
    onClick: (Expense) -> Unit = {}
) {
    val fontSize = 18.sp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 50.dp) // 最小高さを指定
            .clickable { onClick(expense) }
            .padding(10.dp)
    ) {
        val localDateTime: LocalDateTime? = AppTimeZone.isoStringToLocalDateTime(expense.datetime)
        if (localDateTime == null) {
            Text(modifier = Modifier.weight(1f), text = "datetime error", fontSize = fontSize)
        } else {
            val year = localDateTime.year
            val month = localDateTime.monthValue
            val day = localDateTime.dayOfMonth
            Text("$year/$month/$day", modifier = Modifier.weight(1f), fontSize = fontSize)
        }
        Text("${expense.amount}円", modifier = Modifier.weight(1f), fontSize = fontSize)

        val generatedType: String? = expense.generatedType
        if (generatedType == null) {
            Text("タイプエラー", modifier = Modifier.weight(1f), fontSize = fontSize)
        } else {
            val (mainType, subType) = convertGeneratedTypeToDisplayName(generatedType)
            Text(
                if (subType != null) "${mainType}/${subType}" else mainType,
                modifier = Modifier.weight(1f),
                fontSize = fontSize
            )
        }
    }
}