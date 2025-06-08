package gaku.original.myapplication.ui.view.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import gaku.original.myapplication.ui.common.BottomBarView
import gaku.original.myapplication.ui.common.TopBarView

@Composable
fun GraphView(navController: NavController) {
    Scaffold(
        topBar = {
            TopBarView("GraphView作成中")
        },

        bottomBar = { BottomBarView(navController) }
    ) { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            Text("The App will never be completed. \nIt will continue to grow as long as there is imagination left in the world.")
        }
    }
}