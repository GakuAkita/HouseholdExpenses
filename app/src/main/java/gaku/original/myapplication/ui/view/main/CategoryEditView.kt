package gaku.original.myapplication.ui.view.main

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
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
    var editedCategory by remember { mutableStateOf(Category(name = null)) }
    var showDialog by remember { mutableStateOf(false) }

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
                    showDialog = true
                    editedCategory = Category(
                        name = null
                    )
//                    viewModel.addCategory(
//                        sampleCategory,
//                        onAlreadyExists = {
//                            Toast.makeText(context, "The Category Already Exists", Toast.LENGTH_SHORT).show()
//                        }
//                    )
                }
            ) {
                Text("Add Category")
            }

            //ダイアログを表示
            if(showDialog){
                //EditかAddはeditedCategoryのidがnullかどうかで判断する
                CategoryAddEditDialog(
                    category = editedCategory,
                    onSave = {newCategory ->
                        if(newCategory.id == null){
                            viewModel.addCategory(
                                newCategory,
                                onAlreadyExists = {
                                    Toast.makeText(context, "The Category Already Exists", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }else{

                        }
                        viewModel.addCategory(
                            it,
                            onAlreadyExists = {
                                Toast.makeText(context, "The Category Already Exists", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onDismiss = {
                        showDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun CategoryItem(category:Category,onClick:()->Unit = {}){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
    ){
        Text(category.name?:CATEGORY_NULL_REPLACEMENT)
    }
}

@Composable
fun CategoryAddEditDialog(
    category:Category,
    onSave : (category:Category)->Unit,
    onDismiss : ()->Unit,
){
    var newCategory by remember{ mutableStateOf(category) }

    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth()
            ){
                Button(
                    modifier = Modifier
                        .padding(start = 10.dp),
                    onClick = { onDismiss() }
                ){
                    Text("Cancel")
                }
                Button(
                    modifier=Modifier
                        .padding(end = 10.dp),
                    onClick = {
                        /* ここでnewCategoryが適切かチェックする */
                        /* すでにカテゴリーの中に存在するかはここではチェックしない */
                        if(newCategory.name == null||newCategory.name == ""){
                            /* callbackする */
                        } else{
                            onSave(newCategory)
                        }
                    }
                ){
                    Text("Save")
                }
            }
        },
        title = {
            if(newCategory.id == null){
                Text("Add Category")
            }else{
                Text("Edit Category")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = newCategory.name ?: "",
                    onValueChange = {
                        newCategory = newCategory.copy(name = it)
                    }
                )
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = true
        )
    )
}