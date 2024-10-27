package gaku.original.myapplication.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import gaku.original.myapplication.ExpenseViewModel
import gaku.original.myapplication.data.Expense
import kotlinx.coroutines.flow.distinctUntilChanged
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings

@Composable
fun MainView(viewModel: ExpenseViewModel){
    val topBarName ="What is essential is invisible to the eye"
    val listState = rememberLazyListState() // 追加

    //カレンダー横スクロールのため
    val calendarHorizontalInitialPage = 12
    val calendarPagerState= rememberPagerState(initialPage = calendarHorizontalInitialPage){ 2*calendarHorizontalInitialPage + 1 }//前後12ヶ月と現在の月=25ページ
    var previousCalendarPage by remember { mutableStateOf(calendarPagerState.currentPage) }

    Scaffold(
        topBar = {
            TopBarView(topBarName)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* 支出を加える */ },
                containerColor = Color.LightGray,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier=Modifier.size(80.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Button",modifier=Modifier.size(36.dp))
            }
        },
        bottomBar = { BottomBarView() }
    ){
        innerPadding ->
        Column (modifier = Modifier.fillMaxSize().padding(innerPadding)){
            Row (
                modifier = Modifier.fillMaxWidth().padding(start=10.dp),
                ){
                Text("${viewModel.getCalendarYear()}-${viewModel.getCalendarMonth()}")
                Text("Page:${calendarPagerState.currentPage}　previous Page:${previousCalendarPage}")
            }

            //Recompositionされたかどうかのチェック
            //Log.d("Check Recomposition","Calendar Recomposition: ${LocalTime.now()}")
            Row(
                modifier = Modifier.fillMaxWidth(),
            ){
                HorizontalPager(
                    state = calendarPagerState,
                    modifier = Modifier.weight(1f)
                ) {page->
//                    val monthOffset = page-12
                    CalendarDisplay(
                        calendarYear = viewModel.getCalendarYear(),
                        calendarMonth = viewModel.getCalendarMonth())
                }
            }

            /**************************************************/
            /*カレンダーをスクロールしたときにviewModel内の日付を変更する*/
            /**************************************************/
            LaunchedEffect(calendarPagerState) {
                snapshotFlow { calendarPagerState.currentPage }
                    .distinctUntilChanged()
                    .collect{ currentPage->
                        when{
                            currentPage > previousCalendarPage -> viewModel.incrementMonth()
                            currentPage < previousCalendarPage -> viewModel.decrementMonth()
                        }
                        previousCalendarPage=currentPage
//
//                        //ページが範囲を超えた場合、カレンダーをリセット
//                        if (currentPage <= 0 || currentPage >= 2*calendarHorizontalInitialPage ){
//                            viewModel.resetMonthOffset()
//                            calendarPagerState.scrollToPage(calendarHorizontalInitialPage)
//                        }
                }
            }

            //スペースちょっとあける。
            Spacer(modifier=Modifier.padding(10.dp))

            Row (modifier=Modifier.fillMaxWidth()){
                LazyColumnScrollbar(//外部ライブラリ
                    state=listState,
                    settings = ScrollbarSettings.Default
                ) {
                    LazyColumn(
                        state=listState,
                        modifier=Modifier
                            .fillMaxWidth(),
                        userScrollEnabled = true
                    ){
                        items(viewModel.getMonthExpenses()){
                                expense ->
                            ExpenseItem(expense = expense, onEdit = {})
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseItem(expense:Expense, onEdit: () -> Unit){
    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)){
        Text(
            text = "${expense.datetime.dayOfMonth}日",
            modifier=Modifier.weight(1f),
            textAlign = TextAlign.Left//左寄せ
        )
        Text(
            text="${expense.expense}",
            modifier=Modifier.weight(1f),
            textAlign = TextAlign.Left
        )
        Text(
            text=expense.category,
            modifier=Modifier.weight(1f),
            textAlign = TextAlign.Left
        )
    }
}