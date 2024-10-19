package gaku.original.myapplication.ui.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MainView(){
    val topBarName:String="What is essential is invisible to the eye"

    Scaffold(
        topBar = {
            AppBarView(topBarName)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* 家計簿を加える */ },
                containerColor = Color.LightGray,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier=Modifier.size(80.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Button",modifier=Modifier.size(36.dp))
            }
        }
    ){
        innerPadding ->
        //カレンダー
        //日付を押すことでAdd用のFloatingボタン

        //LazyColumn

        Text("This is Main", modifier = Modifier.padding(innerPadding))
    }
}