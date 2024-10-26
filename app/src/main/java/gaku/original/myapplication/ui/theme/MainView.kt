package gaku.original.myapplication.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import gaku.original.myapplication.data.DummyExpenses
import gaku.original.myapplication.data.Expense
import org.w3c.dom.Text


@Composable
fun MainView(){
    val topBarName ="What is essential is invisible to the eye"
    val listState = rememberLazyListState() // 追加

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
            Text("2024-10")
            Row(modifier = Modifier.fillMaxWidth()){
                CalendarDisplay(2024,10)
            }

            //スペースちょっとあける。
            Spacer(modifier=Modifier.padding(10.dp))

            Row (modifier=Modifier.fillMaxWidth()){
                LazyColumn(
                    state=listState,
                    modifier=Modifier
                        .fillMaxWidth()
                        .verticalScrollbar(
                            listState,
                            barTint = Color.Gray,
                            backgroundColor = Color.LightGray
                        ),
                    userScrollEnabled = true
                    ){
                    items(DummyExpenses.expensesList){
                            expense ->
                        ExpenseItem(expense = expense, onEdit = {})
                    }
                }
            }
            Text("End")
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


//スクロールバーの実装。以下のURLを丸パクリ
//https://gaprot.jp/2024/07/22/jetpack-compose-lazylist-scrollbar/
@Composable
fun Modifier.verticalScrollbar(
    state: LazyListState,
    width: Float = 12f,
    barTint: Color = Color.Blue,
    backgroundColor: Color = Color.Gray,
): Modifier {
    return drawWithContent {
        drawContent()

        val firstVisibleElementIndex = state.layoutInfo.visibleItemsInfo.firstOrNull()?.index

        firstVisibleElementIndex?.let {
            val scrollableItems = state.layoutInfo.totalItemsCount - state.layoutInfo.visibleItemsInfo.size
            val scrollbarHeight = this.size.height / scrollableItems
            val offsetY = ((this.size.height - scrollbarHeight) * it) / scrollableItems

            // スクロールバーの背景部分
            drawRoundRect(
                color = backgroundColor,
                topLeft = Offset(x = this.size.width - width, y = 0f),
                size = Size(width, this.size.height),
                cornerRadius = CornerRadius(width / 2, width / 2),
                alpha = 1f
            )

            // スクロールバーの本体部分
            drawRoundRect(
                color = barTint,
                topLeft = Offset(x = this.size.width - width, y = offsetY),
                size = Size(width, scrollbarHeight - 60f),
                cornerRadius = CornerRadius(width / 2, width / 2),
                alpha = 1f
            )
        }
    }
}

