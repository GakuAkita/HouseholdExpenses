package gaku.original.myapplication.ui.theme

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import gaku.original.myapplication.AddEditViewModel
import gaku.original.myapplication.Screen
import java.time.format.DateTimeFormatter

/*
・まずはすべて手入力で実装する
FloatingActionボタンから来た場合は、ボタンを叩いた時間を入力
カレンダーの日付を叩いてきたときはその日付と時間(今の時間)をデフォルトでいれる
 */

@Composable
fun AddEditView(viewModel: AddEditViewModel,navController: NavController){

    Scaffold(
        topBar = {
            //悩みどころだが、BackだとGraphから来たときにGraphに戻る可能性があるので
            //強制的にMainScreenに行くことにする。しっかり設計しないとヒューマンエラー起きそうだな
            TopBarView(
                title = "What is essential is invisible to the eye",
                onBackNavClicked = {
                    navController.navigate(Screen.MainScreen.Content.route)
                },
                navController=navController
            )
        },
        bottomBar = { BottomBarView(navController)}
    ){
        innerPadding ->
        Column(
            modifier=Modifier.fillMaxSize().padding(innerPadding),
            verticalArrangement = Arrangement.Center
            ) {
            val dateFormat=DateTimeFormatter.ofPattern("yyyy/MM/dd")
            val timeFormat=DateTimeFormatter.ofPattern("HH:mm")

            Row (
                modifier=Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Absolute.Left
            ){
                TextField(
                    value = viewModel.datetime.value.format(dateFormat),
                    onValueChange = {},
                    label= {Text(text="Date")},
                    modifier=Modifier.width(150.dp)
                )

                Spacer(modifier=Modifier.padding(8.dp))
                TextField(
                    value = viewModel.datetime.value.format(timeFormat),
                    onValueChange = {},
                    label= {Text(text="Time")},
                    modifier=Modifier.width(100.dp)
                )
            }

            Spacer(modifier=Modifier.padding(8.dp))

            Row(
                modifier=Modifier.fillMaxWidth()
            ) {
                TextField(
                    //数値だけ受け付ける感じにしたい
                    value = "${viewModel.expense.value?:""}",
                    onValueChange ={
                        viewModel.expenseUpdate(it)
                    },
                    label= {Text(text="Expense")},
                    modifier=Modifier.width(260.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            Spacer(modifier=Modifier.padding(8.dp))

            Row(
                modifier=Modifier.fillMaxWidth()
            ){
                //カテゴリー(選択肢から選んでもらいたい。RoomDB?)
                //@Todo タップしたら画面右からスライドして選択肢が入った列が出てくる感じ
                TextField(
                    value=viewModel.category.value?:"",
                    onValueChange = {
                    },
                    modifier=Modifier.width(260.dp),
                    label={Text(text="Category")}
                )
            }

            Spacer(modifier=Modifier.padding(8.dp))

            Row(
                modifier=Modifier.fillMaxWidth()
            ){
                //メモ
                TextField(
                    value=viewModel.note.value?:"",
                    onValueChange = {
                        viewModel.noteUpdate(it)
                    },
                    modifier=Modifier.width(260.dp),
                    label={Text(text="Note")},
                    singleLine = false
                )
            }

            Button(
                onClick = {
                    Log.d("AddEdit","Button Clicked")
                },
                modifier=Modifier.fillMaxWidth()
            ) {
                Text("ボタン")
            }
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
