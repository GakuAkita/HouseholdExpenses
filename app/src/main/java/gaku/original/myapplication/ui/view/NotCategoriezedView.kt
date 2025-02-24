package gaku.original.myapplication.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

/* 使わない..... */
@Composable
fun NotCategorizedView(navController: NavController){
    Scaffold(
        topBar = {
            TopBarView("NotCategorizedView作成中")
        },

        bottomBar = { BottomBarView(navController) }
    ){
        innerPadding->
        Column(
            modifier= Modifier.fillMaxSize().padding(innerPadding)
        ) {
            Text("The App will never be completed.\n" +
                    " It will continue to grow as long as there is imagination left in the world.")
        }
    }
}