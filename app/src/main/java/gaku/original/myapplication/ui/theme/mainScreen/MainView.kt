package gaku.original.myapplication.ui.theme.mainScreen

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.navigation.NavHostController
import gaku.original.myapplication.ExpenseViewModel
import gaku.original.myapplication.Screen
import gaku.original.myapplication.data.Expense
import gaku.original.myapplication.toLocalDateTime
import gaku.original.myapplication.ui.theme.BottomBarView
import gaku.original.myapplication.ui.theme.CalendarDisplay
import gaku.original.myapplication.ui.theme.TopBarView
import kotlinx.coroutines.flow.distinctUntilChanged
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun MainView(viewModel: ExpenseViewModel,navController: NavHostController){
    val topBarName ="What is essential is invisible to the eye"
    val listState = rememberLazyListState()

    //カレンダー横スクロールのため
    val calendarHorizontalInitialPage = 12
    val calendarPagerState= rememberPagerState(initialPage = calendarHorizontalInitialPage){ 2*calendarHorizontalInitialPage + 1 }//前後12ヶ月と現在の月=25ページ
    var previousCalendarPage by remember { mutableIntStateOf(calendarPagerState.currentPage) }

    //StateFlowの状態を監視しないとページを変えたときにカレンダーの年や月が変わらない
    val monthOffset by viewModel.monthOffset.collectAsState()//monthOffset StateFlowを監視


    var currentPageMonth = viewModel.getCalendarMonth()
    var currentPageYear = viewModel.getCalendarYear()
    // monthOffsetが変更されたときに再実行する処理
    LaunchedEffect(monthOffset) {
        // monthOffsetが変更されたときに再実行する処理をここに書く
        currentPageMonth = viewModel.getCalendarMonth()
        currentPageYear = viewModel.getCalendarYear()

        viewModel.filterExpensesByMonth()
    }

    //サインインした瞬間にやるべきこと
    LaunchedEffect(Unit) {
        if(viewModel.addObserveExpensesDoneFlagState.value==false){
            viewModel.fetchAllExpenses(
                onComplete = {
                    viewModel.initializeLastFetchedTime(
                        onSet = {
                            viewModel.observeExpenses()
                            viewModel.setAddObserveExpensesDoneFlag(true)
                            Log.d("MainView","observeExpenses called.")
                        }
                    )
                }
            )
        }
        else{
            Log.d("MainView","addObserveExpenses is already done.")
        }
    }

    val monthExpenses = viewModel.filteredExpenses.collectAsState().value
    Log.d("MainView","monthExpenses updated in MainView:: size=${monthExpenses.size}")

    Scaffold(
        topBar = {
            TopBarView(topBarName)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    //必ずAddなのでリセットで
                    viewModel.resetExpenseInstance()
                    /* Addに飛ぶ */
                    navController.navigate(Screen.MainScreen.AddEdit.route)
                },
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                contentColor = MaterialTheme.colorScheme.onSecondary,
                shape = CircleShape,
                modifier=Modifier.size(80.dp),
                elevation = FloatingActionButtonDefaults.elevation(0.dp)//デフォルトだとElevationがついているっぽい。
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Button",modifier=Modifier.size(36.dp))
            }
        },
        bottomBar = { BottomBarView(navController) }
    ){
            innerPadding ->
        Column (modifier = Modifier.fillMaxSize().padding(innerPadding)){
            Row (
                modifier = Modifier.fillMaxWidth().padding(start=10.dp),
            ){
                Text("${currentPageYear}-${currentPageMonth}")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
            ){
                HorizontalPager(
                    state = calendarPagerState,
                    modifier = Modifier.weight(1f)
                ) {
                    CalendarDisplay(
                        calendarYear = viewModel.getCalendarYear(),
                        calendarMonth = viewModel.getCalendarMonth(),
                        monthExpenses = monthExpenses,
                        onDayClicked = {day->
                            val inputDate:LocalDate=day.date
                            val inputTime:LocalTime = LocalTime.now()//今の時間でもいいし、00:00:00でもいいな
                            //リセットして
                            viewModel.resetExpenseInstance()
                            //日付を入力
                            Log.d("Akita Debug","$inputDate")
                            viewModel.updateExpenseInstanceDate(inputDate)
                            //時間を入力
                            viewModel.updateExpenseInstanceTime(inputTime)
                            //AddEditViewに移動
                            navController.navigate(Screen.MainScreen.AddEdit.route)
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
                    .collect{ currentPage->
                        Log.d("MainView","Calendar pager state changed: $currentPage")
                        when{
                            currentPage > previousCalendarPage -> viewModel.incrementMonth()
                            currentPage < previousCalendarPage -> viewModel.decrementMonth()
                        }
                        previousCalendarPage=currentPage
                    }
            }

            //スペースちょっとあける。
            Spacer(modifier=Modifier.padding(10.dp))

            Row (modifier=Modifier.fillMaxWidth()){
                LazyColumnScrollbar(//外部ライブラリ
                    state=listState,
                    settings = ScrollbarSettings.Default.copy(
                        alwaysShowScrollbar = true,
                        thumbUnselectedColor = MaterialTheme.colorScheme.secondary,
                        thumbSelectedColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    LazyColumn(
                        state=listState,
                        modifier=Modifier
                            .fillMaxWidth(),
                        userScrollEnabled = true
                    ){
                        items(monthExpenses){
                                expense ->
                            ExpenseItem(
                                expense = expense,
                                onEdit = {
                                    //viewModel内の値を転写
                                    viewModel.updateExpenseInstance(expense)
                                    //AddEditViewに移動
                                    navController.navigate(Screen.MainScreen.AddEdit.route)
                                })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseItem(expense:Expense, onEdit: () -> Unit){
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(32.dp)
        .border(width=1.dp, color = MaterialTheme.colorScheme.onSecondary)
        .clickable {
            onEdit()
        },
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(
            text = "${toLocalDateTime(expense.datetime)?.dayOfMonth }日",
            modifier=Modifier.weight(1f),
            textAlign = TextAlign.Left//左寄せ
        )
        Text(
            text="${expense.amount}円",
            modifier=Modifier.weight(1f),
            textAlign = TextAlign.Left
        )
        Text(
            text=expense.category?:"",
            modifier=Modifier.weight(1f),
            textAlign = TextAlign.Left
        )
    }
}