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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import gaku.original.myapplication.data.DummyExpenses


@Composable
fun MainView(){
    val topBarName ="What is essential is invisible to the eye"

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

            Row {
                LazyColumn(modifier=Modifier.fillMaxWidth()){
                    items(DummyExpenses.expensesList){
                            expense ->
                        Text(
                            text = "${expense.category} ${expense.expense}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            Text("End")
        }
    }
}