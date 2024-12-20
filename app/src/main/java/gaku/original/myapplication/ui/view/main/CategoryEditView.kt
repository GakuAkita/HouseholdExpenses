package gaku.original.myapplication.ui.view.main

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import gaku.original.myapplication.Screen
import gaku.original.myapplication.data.CATEGORY_NULL_REPLACEMENT
import gaku.original.myapplication.data.Category
import gaku.original.myapplication.ui.view.BottomBarView
import gaku.original.myapplication.ui.view.TopBarView
import gaku.original.myapplication.viewModel.ExpenseSharedViewModel

@Composable
fun CategoryAddEditView(
    viewModel:ExpenseSharedViewModel,
    navController: NavController
){
    val context = LocalContext.current
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

            val sampleCategory = Category(
                name = "Food1",
            )
            for(category in viewModel.allCategories.collectAsState(initial = emptyList()).value){
                CategoryItem(category = category)
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    viewModel.addCategory(
                        sampleCategory,
                        onAlreadyExists = {
                            Toast.makeText(context, "The Category Already Exists", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            ) {
                Text("Add Category")
            }
        }
    }
}

@Composable
fun CategoryItem(category:Category){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                //クリックして編集
            }
    ){
        Text(category.name?:CATEGORY_NULL_REPLACEMENT)
    }
}