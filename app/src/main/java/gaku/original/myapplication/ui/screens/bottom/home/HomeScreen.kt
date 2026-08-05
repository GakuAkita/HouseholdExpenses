package gaku.original.myapplication.ui.screens.bottom.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.data.AppTimeZone
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.viewModel.main.ExpenseListViewModel

@Composable
fun HomeScreen(

    bottomNavController: NavHostController
){

}

@Composable
fun MainView(
    viewModel: ExpenseListViewModel = hiltViewModel(),
    navController: NavHostController
) {
//    val viewName = "MainView"
//
//    val topBarName = "What is essential is invisible to the eye"
//    val listState = rememberLazyListState()
//
//    //この画面のルート。AddEditScreenに行くときに必要
//    val originalRoute = Screen.MainScreen.Content
//
//    //カレンダー横スクロールのため
//    val calendarHorizontalInitialPage = 12
//    /* 最初のページは、現在時刻と */
//    val calendarPagerState =
//        rememberPagerState(initialPage = 12) { 2 * calendarHorizontalInitialPage + 1 }//前後12ヶ月と現在の月=25ページ
//    var previousCalendarPage by remember { mutableIntStateOf(calendarPagerState.currentPage) }
//
//    //StateFlowの状態を監視しないとページを変えたときにカレンダーの年や月が変わらない
//    val monthOffset by viewModel.monthOffset.collectAsState()//monthOffset StateFlowを監視
//    val monthTotalExpense by viewModel.monthTotalExpense.collectAsState()
//    val monthlyEstimatedExpense by viewModel.monthlyEstimatedExpense.collectAsState()
//
//    var currentPageMonth = viewModel.getCalendarMonth()
//    var currentPageYear = viewModel.getCalendarYear()
//    // monthOffsetが変更されたときに再実行する処理
//    LaunchedEffect(monthOffset) {
//        LogAkitaDebug("monthOffset is changed:$monthOffset currentPageMonth:$currentPageMonth currentPageYear:$currentPageYear")
//        // monthOffsetが変更されたときに再実行する処理をここに書く
//        currentPageMonth = viewModel.getCalendarMonth()
//        currentPageYear = viewModel.getCalendarYear()
//
//        if (monthOffset > MONTH_RANGE || monthOffset < -MONTH_RANGE) {
//            /* ログインしたてはmonthOffsetが0なので、ここに入らない！！ */
//
//            /* 中心の月から外れたら、storedExpensesを更新する */
//            viewModel.updateCenterCalendarDate(currentPageYear, currentPageMonth)//カレンダーの中心を更新
//            viewModel.resetMonthOffset()//オフセットをリセット
//
//            LogAkitaDebug("ローカルのExpenseを更新します！！")
//
//            //リスナーの管理も中でやる
//            viewModel.updateStoredExpenses(
//                currentPageYear,
//                currentPageMonth,
//                callback = { statusInfo ->
//                    when (statusInfo.status) {
//                        FuncStatus.SUCCESS -> {
//                        }
//
//                        FuncStatus.TIMEOUT -> {
//                            /* スナックバーを出したい */
//                        }
//
//                        FuncStatus.FAILED -> {
//                            /* スナックバーを出したい。 */
//                        }
//
//                        else -> {}
//                    }
//                }
//            )
//        } /* Expensesの更新が必要なときはtrueにする */
//
//        LogAkitaDebug("これ走ってる？？")
//        viewModel.filterExpensesByMonth()
//    }
//
//    LaunchedEffect(Unit) {
//        /* 内部で一回だけ実行するようにしている、、 */
//        viewModel.onSignedIn(callback = { status ->
//            when (status.status) {
//                FuncStatus.SUCCESS -> {
//                    Log.d(viewName, "サインイン直後にやる処理に成功")
//                }
//
//                FuncStatus.TIMEOUT -> {
//                    Log.d(viewName, "サインイン直後にやる処理がタイムアウトしました")
//                }
//
//                FuncStatus.FAILED -> {
//                    Log.d(viewName, "サインイン直後に破る処理に失敗しました")
//                }
//
//                FuncStatus.WARNING -> {}
//            }
//        })
//    }
//
//    LaunchedEffect(Unit) {
//        /**
//         * すでにonCreateが起動しているがログインしていないときログイン→メイン画面を通ってOCR画面を起動
//         * ログインしていないときはメインを経由するので、ここからOCRを起動する
//         */
//        if (viewModel.isShouldMoveToOCR()) {
//            viewModel.setIsMovedToOCR()
//            navigateToOCREntryView(navController)
//        }
//
//        /* Notificationから来たときはここでNavigateする */
//        if (viewModel.isShouldMoveToNLProcess()) {
//            /**
//             * あ～ここで2回navigateしてしまってるってことか？？
//             */
//            viewModel.setIsMovedToNLProcess()
//            navigateToNLProcess(navController)
//        }
//    }
//
//
//    //rememberをつけると再コンポーズのとき無駄に走らない
//    val monthExpenses by remember { viewModel.filteredExpenses }.collectAsState(initial = emptyList())
//    val expensesLoadingStatus by remember { viewModel.expensesLoadingStatus }.collectAsState()
//
//    Scaffold(
//        topBar = {
//            TopBarView(topBarName)
//        },
//        floatingActionButton = {
//            FloatingActionButtonWithIcon(
//                onClick = {
//                    //必ずAddなのでリセットで
//                    viewModel.resetTmpExpense()
//
//                    /*月は現在のカレンダーの時間をいれる??*/
//                    /* Addに飛ぶ */
//                    navController.navigate(
//                        Screen.GlobalScreen.ExpenseAddEdit.createRoute(
//                            originalRoute
//                        )
//                    )
//                }
//            )
//        },
//        bottomBar = { BottomBarView(navController) }
//    ) { innerPadding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding)
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(start = 10.dp),
//            ) {
//                Text("${currentPageYear}-${currentPageMonth}")
//                Spacer(modifier = Modifier.padding(10.dp))
//                Column {
//                    Text("Monthly Total:${monthTotalExpense}")
//                    if (monthOffset == 0) {/* monthOffset=0とは今日の日付ってこと */
//                        Text("Estimated:${monthlyEstimatedExpense}")
//                    }
//                }
//            }
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//            ) {
//                HorizontalPager(
//                    state = calendarPagerState,
//                    modifier = Modifier.weight(1f)
//                ) {
//                    CalendarDisplay(
//                        calendarYear = viewModel.getCalendarYear(),
//                        calendarMonth = viewModel.getCalendarMonth(),
//                        monthExpenses = viewModel.filteredExpenses.collectAsState().value,
//                        onDayClicked = { day ->
//                            /* カレンダー */
//                            val year: Int = day.date.year
//                            val month: Int = day.date.monthValue
//                            val dayInt: Int = day.date.dayOfMonth
//
//                            /* UTCの時間を設定の時間に変換して、それをLocalTimeにする */
//                            val zoneDateTime = AppTimeZone.getCurrentTimeInZone()
//
////                            Log.d(
////                                "Debug",
////                                "year=${year::class}, month=${month::class}, day=${day::class}"
////                            )
////                            Log.d(
////                                "Debug",
////                                "hour=${zoneDateTime.hour::class}, minute=${zoneDateTime.minute::class}, second=${zoneDateTime.second::class}"
////                            )
//
//                            val inputDateTime = LocalDateTime.of(
//                                year,
//                                month,
//                                dayInt,
//                                zoneDateTime.hour,
//                                zoneDateTime.minute,
//                                zoneDateTime.second
//                            )
//
//                            Log.d("Akita Debug", "$inputDateTime")
//                            /**
//                             * このnewDatetimeは設定のタイムゾーンの日付で、
//                             * ViewModel内でUTCに変換する
//                             */
//                            /**
//                             * このnewDatetimeは設定のタイムゾーンの日付で、
//                             * ViewModel内でUTCに変換する
//                             */
//                            viewModel.setToTmpExpenseFromCalendar(inputDateTime)
//                            //ExpenseAddEditViewに移動
//                            navController.navigate(
//                                Screen.GlobalScreen.ExpenseAddEdit.createRoute(
//                                    originalRoute
//                                )
//                            )
//                        }
//                    )
//                }
//            }
//
//            /**************************************************/
//            /*カレンダーをスクロールしたときにviewModel内の日付を変更する*/
//            /**************************************************/
//            LaunchedEffect(calendarPagerState) {
//                snapshotFlow { calendarPagerState.currentPage }
//                    .distinctUntilChanged()
//                    .collect { currentPage ->
//                        Log.d(viewName, "Calendar pager state changed: $currentPage")
//                        when {
//                            currentPage > previousCalendarPage -> viewModel.incrementMonth()
//                            currentPage < previousCalendarPage -> viewModel.decrementMonth()
//                        }
//                        previousCalendarPage = currentPage
//                    }
//            }
//
//            //スペースちょっとあける。
//            Spacer(modifier = Modifier.padding(10.dp))
//
//            when (expensesLoadingStatus) {
//                LoadingStatus.LOADING -> {
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.Center
//                    ) { CircularProgressIndicator() }
//                }
//
//                LoadingStatus.TIMEOUT -> {
//                    Text("Timeout!!!")
//                    Button(
//                        onClick = {
//                            viewModel.updateStoredExpenses(
//                                currentPageYear,
//                                currentPageMonth,
//                                callback = {}
//                            )
//                        },
//                    ) { Text("再読み込み") }
//                }
//
//                LoadingStatus.ERROR -> {
//                    Text("Unable to get Expenses properly. Please contact the developer.")
//                    Button(onClick = {
//                        viewModel.updateStoredExpenses(
//                            currentPageYear,
//                            currentPageMonth,
//                            callback = {}
//                        )
//                    }) { Text("再読み込み") }
//                }
//
//                else -> {
//                    /* Do nothing */
//                }
//            }
//            Row(modifier = Modifier.fillMaxWidth()) {
//                LazyColumnScrollbar(//外部ライブラリ
//                    state = listState,
//                    settings = ScrollbarSettings.Default.copy(
//                        alwaysShowScrollbar = true,
//                        thumbUnselectedColor = MaterialTheme.colorScheme.secondary,
//                        thumbSelectedColor = MaterialTheme.colorScheme.primary
//                    )
//                ) {
//                    LazyColumn(
//                        state = listState,
//                        modifier = Modifier
//                            .fillMaxWidth(),
//                        userScrollEnabled = true
//                    ) {
//                        items(monthExpenses) { expense ->
//                            ExpenseItem(
//                                expense = expense,
//                                isToday = AppTimeZone.isoStringToLocalDateTime(expense.datetime)
//                                    ?.let { expenseDate ->
//                                        val current = AppTimeZone.getCurrentTimeInZone()
//                                        expenseDate.year == current.year && expenseDate.month == current.month && expenseDate.dayOfMonth == current.dayOfMonth
//                                    } == true,
//                                onEdit = {
//                                    print("onEdit was tapped...")
//                                    viewModel.resetTmpExpense()
//                                    //viewModel内の値を転写
//                                    viewModel.setToTmpExpense(expense)
//                                    //ExpenseAddEditViewに移動
//                                    navController.navigate(
//                                        Screen.GlobalScreen.ExpenseAddEdit.createRoute(
//                                            originalRoute
//                                        )
//                                    )
//                                })
//                        }
//                    }
//                }
//            }
//        }
//    }
}

@Composable
fun ExpenseItem(expense: Expense, isToday: Boolean = false, onEdit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .border(
                width = 1.dp,
                color = if (isToday) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSecondary
            )
            .then(
                if (isToday) {
                    Modifier.background(
                        color = MaterialTheme.colorScheme.onTertiary,
                        shape = RectangleShape
                    )
                } else {
                    Modifier // ← ここを追加
                }
            )
            .clickable {
                onEdit()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${
                AppTimeZone.isoStringToLocalDateTime(expense.datetime)?.dayOfMonth ?: 0
            }日",
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