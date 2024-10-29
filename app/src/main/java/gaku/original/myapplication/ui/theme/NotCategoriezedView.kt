package gaku.original.myapplication.ui.theme

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun NotCategorizedView(navController: NavController){
    Scaffold(
        topBar = {
            TopBarView("NotCategorizedView作成中")
        },

        bottomBar = { BottomBarView(navController)}
    ){
            innerPadding ->
        Text("${innerPadding}")
    }
}