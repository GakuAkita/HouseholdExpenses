package gaku.original.myapplication.ui.theme

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
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
                    value = viewModel.expenseData.datetime.format(dateFormat),
                    onValueChange = {},
                    label= {Text(text="日付")},
                    modifier=Modifier.width(150.dp)
                )

                Spacer(modifier=Modifier.padding(10.dp))
                TextField(
                    value = viewModel.expenseData.datetime.format(timeFormat),
                    onValueChange = {},
                    label= {Text(text="時間")},
                    modifier=Modifier.width(100.dp)
                )
            }

            Row(
                modifier=Modifier.fillMaxWidth()
            ) {
                Text("金額:")

                //金額
                Text(
                    "${viewModel.expenseData.expense}",
                    modifier=Modifier.clickable {
                        Log.d("AddEdit","Money Clicked")
                    }
                )
            }

            Row(
                modifier=Modifier.fillMaxWidth()
            ){
                //カテゴリー(選択肢から選んでもらいたい。RoomDB?)
                Text(
                    "${viewModel.expenseData.category}",
                    modifier=Modifier.clickable {
                    }
                )
            }

            Row(
                modifier=Modifier.fillMaxWidth()
            ){
                //メモ
                Text(
                    "${viewModel.expenseData.note}",
                    modifier = Modifier.clickable {
                    }
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
