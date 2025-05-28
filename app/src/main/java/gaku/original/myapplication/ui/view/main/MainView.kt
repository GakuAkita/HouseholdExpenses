package gaku.original.myapplication.ui.view.main

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.Screen
import gaku.original.myapplication.Utility.LogAkitaDebug
import gaku.original.myapplication.Utility.toLocalDateTime
import gaku.original.myapplication.data.Constants.MONTH_RANGE
import gaku.original.myapplication.data.Constants.Status.LoadingStatus
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Expense
import gaku.original.myapplication.ui.view.BottomBarView
import gaku.original.myapplication.ui.view.CalendarDisplay
import gaku.original.myapplication.ui.view.TopBarView
import gaku.original.myapplication.viewModel.ExpenseListViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Composable
fun MainView(
    viewModel: ExpenseListViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val viewName = "MainView"

    val topBarName = "What is essential is invisible to the eye"
    val listState = rememberLazyListState()

    //カレンダー横スクロールのため
    val calendarHorizontalInitialPage = 12
    /* 最初のページは、現在時刻と */
    val calendarPagerState =
        rememberPagerState(initialPage = 12) { 2 * calendarHorizontalInitialPage + 1 }//前後12ヶ月と現在の月=25ページ
    var previousCalendarPage by remember { mutableIntStateOf(calendarPagerState.currentPage) }

    //StateFlowの状態を監視しないとページを変えたときにカレンダーの年や月が変わらない
    val monthOffset by viewModel.monthOffset.collectAsState()//monthOffset StateFlowを監視
    val monthTotalExpense by viewModel.monthTotalExpense.collectAsState()

    var currentPageMonth = viewModel.getCalendarMonth()
    var currentPageYear = viewModel.getCalendarYear()
    // monthOffsetが変更されたときに再実行する処理
    LaunchedEffect(monthOffset) {
        LogAkitaDebug("monthOffset is changed:$monthOffset currentPageMonth:$currentPageMonth currentPageYear:$currentPageYear")
        // monthOffsetが変更されたときに再実行する処理をここに書く
        currentPageMonth = viewModel.getCalendarMonth()
        currentPageYear = viewModel.getCalendarYear()

        if (monthOffset > MONTH_RANGE || monthOffset < -MONTH_RANGE) {
            /* ログインしたてはmonthOffsetが0なので、ここに入らない！！ */

            /* 中心の月から外れたら、storedExpensesを更新する */
            viewModel.updateCenterCalendarDate(currentPageYear, currentPageMonth)//カレンダーの中心を更新
            viewModel.resetMonthOffset()//オフセットをリセット

            LogAkitaDebug("ローカルのExpenseを更新します！！")

            //リスナーの管理も中でやる
            viewModel.updateStoredExpenses(
                currentPageYear,
                currentPageMonth,
                callback = { statusInfo ->
                    when (statusInfo.status) {
                        SuspendFuncStatus.SUCCESS -> {
                        }

                        SuspendFuncStatus.TIMEOUT -> {
                            /* スナックバーを出したい */
                        }

                        SuspendFuncStatus.FAILED -> {
                            /* スナックバーを出したい。 */
                        }
                    }
                }
            )
        } /* Expensesの更新が必要なときはtrueにする */

        LogAkitaDebug("これ走ってる？？")
        viewModel.filterExpensesByMonth()
    }

    LaunchedEffect(Unit) {
        /* 内部で一回だけ実行するようにしている、、 */
        viewModel.onSignedIn(callback = { status ->
            when (status.status) {
                SuspendFuncStatus.SUCCESS -> {
                    Log.d(viewName, "サインイン直後にやる処理に失敗しました")
                }

                SuspendFuncStatus.TIMEOUT -> {
                    Log.d(viewName, "サインイン直後にやる処理がタイムアウトしました")
                }

                SuspendFuncStatus.FAILED -> {
                    Log.d(viewName, "サインイン直後に破る処理に失敗しました")
                }

                else -> {
                    Log.d(viewName, "よくわからん処理")
                }
            }
        })
    }

    //rememberをつけると再コンポーズのとき無駄に走らない
    val monthExpenses by remember { viewModel.filteredExpenses }.collectAsState(initial = emptyList())
    //@TODO 特に問題はないのだが、自分が思うよりもmonthExpensesが動いている(配列変わってなくても)ので注意
    //あ～わからんけど、LoadingStateが変わったときとか逐一走ってんのかな、
    Log.d(viewName, "monthExpenses loaded:${monthExpenses.size}")

    val expensesLoadingStatus by remember { viewModel.expensesLoadingStatus }.collectAsState()

    Scaffold(
        topBar = {
            TopBarView(topBarName)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    //必ずAddなのでリセットで
                    viewModel.resetTmpExpense()

                    /*月は現在のカレンダーの時間をいれる??*/
                    /* Addに飛ぶ */
                    navController.navigate(Screen.MainScreen.ExpenseAddEdit.route)
                },
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                contentColor = MaterialTheme.colorScheme.onSecondary,
                shape = CircleShape,
                modifier = Modifier.size(80.dp),
                elevation = FloatingActionButtonDefaults.elevation(0.dp)//デフォルトだとElevationがついているっぽい。
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add Button",
                    modifier = Modifier.size(36.dp)
                )
            }
        },
        bottomBar = { BottomBarView(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp),
            ) {
                Text("${currentPageYear}-${currentPageMonth}")
                Spacer(modifier = Modifier.padding(10.dp))
                Text("Monthly Total:${monthTotalExpense}")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                HorizontalPager(
                    state = calendarPagerState,
                    modifier = Modifier.weight(1f)
                ) {
                    CalendarDisplay(
                        calendarYear = viewModel.getCalendarYear(),
                        calendarMonth = viewModel.getCalendarMonth(),
                        monthExpenses = viewModel.filteredExpenses.collectAsState().value,
                        onDayClicked = { day ->
                            val inputDate: LocalDate = day.date
                            val inputTime: LocalTime = LocalTime.now()//今の時間でもいいし、00:00:00でもいいな
                            val newDatetime: LocalDateTime =
                                inputDate.atTime(inputTime)//LocalDateTimeに変換
                            Log.d("Akita Debug", "$inputDate")
                            viewModel.setToTmpExpenseFromCalendar(newDatetime)
                            //ExpenseAddEditViewに移動
                            navController.navigate(Screen.MainScreen.ExpenseAddEdit.route)
                        }
                    )
                }
            }

            /**************************************************/
            /*カレンダーをスクロールしたときにviewModel内の日付を変更する*/
            /**************************************************/
            LaunchedEffect(calendarPagerState) {
                snapshotFlow { calendarPagerState.currentPage }
                    .distinctUntilChanged()
                    .collect { currentPage ->
                        Log.d(viewName, "Calendar pager state changed: $currentPage")
                        when {
                            currentPage > previousCalendarPage -> viewModel.incrementMonth()
                            currentPage < previousCalendarPage -> viewModel.decrementMonth()
                        }
                        previousCalendarPage = currentPage
                    }
            }

            //スペースちょっとあける。
            Spacer(modifier = Modifier.padding(10.dp))

            if (expensesLoadingStatus == LoadingStatus.LOADING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) { CircularProgressIndicator() }
            } else if (expensesLoadingStatus == LoadingStatus.TIMEOUT) {
                Text("Timeout!!!")
                Button(
                    onClick = {
                        viewModel.updateStoredExpenses(
                            currentPageYear,
                            currentPageMonth,
                            callback = {}
                        )
                    },
                ) { Text("再読み込み") }
            } else if (expensesLoadingStatus == LoadingStatus.ERROR) {
                Text("Unable to get Expenses properly. Please contact the developer.")
                Button(onClick = {
                    viewModel.updateStoredExpenses(
                        currentPageYear,
                        currentPageMonth,
                        callback = {}
                    )
                }) { Text("再読み込み") }
            } else {
                /* Do nothing */
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                LazyColumnScrollbar(//外部ライブラリ
                    state = listState,
                    settings = ScrollbarSettings.Default.copy(
                        alwaysShowScrollbar = true,
                        thumbUnselectedColor = MaterialTheme.colorScheme.secondary,
                        thumbSelectedColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth(),
                        userScrollEnabled = true
                    ) {
                        items(monthExpenses) { expense ->
                            ExpenseItem(
                                expense = expense,
                                onEdit = {
                                    print("onEdit was tapped...")
                                    //viewModel内の値を転写
                                    viewModel.setToTmpExpense(expense)
                                    //ExpenseAddEditViewに移動
                                    navController.navigate(Screen.MainScreen.ExpenseAddEdit.route)
                                })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseItem(expense: Expense, onEdit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.onSecondary)
            .clickable {
                onEdit()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${toLocalDateTime(expense.datetime)?.dayOfMonth}日",
            modifier = Modifier.weight(1f),
            fontSize = 20.sp,
            textAlign = TextAlign.Left//左寄せ
        )
        Text(
            text = "${expense.amount}円",
            modifier = Modifier.weight(1f),
            fontSize = 20.sp,
            textAlign = TextAlign.Left
        )
        Text(
            text = expense.category?.name ?: "",
            modifier = Modifier.weight(1f),
            fontSize = 20.sp,
            textAlign = TextAlign.Left
        )
    }
}