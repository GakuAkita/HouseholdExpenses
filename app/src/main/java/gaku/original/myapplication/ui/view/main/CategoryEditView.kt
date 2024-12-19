package gaku.original.myapplication.ui.view.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import gaku.original.myapplication.Screen
import gaku.original.myapplication.ui.view.BottomBarView
import gaku.original.myapplication.ui.view.TopBarView
import gaku.original.myapplication.viewModel.ExpenseSharedViewModel

@Composable
fun CategoryEditView(
    viewModel:ExpenseSharedViewModel,
    navController: NavController
){

    Scaffold(
        topBar = {
            //悩みどころだが、BackだとGraphから来たときにGraphに戻る可能性があるので
            //強制的にMainScreenに行くことにする。しっかり設計しないとヒューマンエラー起きそうだな
            TopBarView(
                title = "What is essential is invisible to the eye",
                onBackNavClicked = {
                    navController.navigate(Screen.MainScreen.CategoryAddEdit.route)
                },
                navController=navController
            )
        },
        bottomBar = { BottomBarView(navController) }
    ){innerPadding->
        Column(
            modifier= Modifier.padding(innerPadding)
        ) {
            Column

        }
    }
}