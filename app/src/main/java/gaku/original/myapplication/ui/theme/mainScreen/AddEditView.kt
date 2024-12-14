package gaku.original.myapplication.ui.theme.mainScreen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import gaku.original.myapplication.viewModel.ExpenseViewModel
import gaku.original.myapplication.Screen
import gaku.original.myapplication.ui.theme.BottomBarView
import gaku.original.myapplication.ui.theme.TopBarView
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/*
・まずはすべて手入力で実装する
FloatingActionボタンから来た場合は、ボタンを叩いた時間を入力
カレンダーの日付を叩いてきたときはその日付と時間(今の時間)をデフォルトでいれる
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditView(viewModel: ExpenseViewModel, navController: NavController){
    //Toastとか用
    val context= LocalContext.current

    //日付、時間の選択肢用
    var isDatePickerVisible by remember { mutableStateOf(false) }
    var isTimePickerVisible by remember { mutableStateOf(false) }

    //enabled=falseにしても同じ色のスタイルを保持したい。色のセットを保存しておく
    val enabledTextFiledColorSet=TextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant, // 無効化時のインジケーター色を変更
        disabledTextColor = MaterialTheme.colorScheme.onSurface, // テキスト色を維持
        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    var categoryOptionsExpanded by remember { mutableStateOf(false) }

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
        bottomBar = { BottomBarView(navController) }
    ){
        innerPadding ->
        Column(
            modifier=Modifier.fillMaxSize().padding(innerPadding),
            verticalArrangement = Arrangement.Center
            ) {
            val dateFormat=DateTimeFormatter.ofPattern("yyyy/MM/dd")
            val timeFormat=DateTimeFormatter.ofPattern("HH:mm")

            /*************************************************/
            /* 日付の項目 */
            /*************************************************/
            Row (
                modifier=Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Absolute.Left
            ){
                TextField(
                    value = viewModel.getExpenseInstanceDateTime()?.format(dateFormat) ?: "日付が入っていません",
                    onValueChange = {},
                    enabled = false,
                    readOnly = true,
                    label= {Text(text="Date")},
                    modifier=Modifier
                        .width(150.dp)
                        .clickable {
                            isDatePickerVisible=true
                        },
                    colors= enabledTextFiledColorSet
                )
                /* 日付をクリックしたときにどうなるか */
                if (isDatePickerVisible) {
                    DatePickerModal(
                        onDateSelected = { dateMillis ->
                            // 選択された日付を処理
                            dateMillis?.let {
                                val selectedDate = LocalDateTime
                                    .ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
                                    .toLocalDate() // 日付部分のみ取得

                                // 選択された日付で Expense インスタンスを更新
                                viewModel.updateExpenseInstanceDate(selectedDate)
                            }
                        },
                        onDismiss = { isDatePickerVisible = false }
                    )
                }

                Spacer(modifier=Modifier.padding(8.dp))

                TextField(
                    value = viewModel.getExpenseInstanceDateTime()?.format(timeFormat) ?:"時間が入っていません",
                    onValueChange = {},
                    enabled=false,
                    readOnly = true,
                    label= { Text(text="Time") },
                    modifier=Modifier
                        .width(100.dp)
                        .clickable {
                            isTimePickerVisible=true
                        },
                    colors=enabledTextFiledColorSet
                )

                //時間をタップしたらダイアログを表示して選択させる
                //Clickableの中身はComposable関数を入れられないらしい？だからここで分けて書いている
                if(isTimePickerVisible){
                    //nullでないときのみ時刻を表示
                    viewModel.getExpenseInstanceDateTime()?.let {
                        DialWithDialog(
                            onConfirm = {selectedTime->
                                // 選択した時間を取得して ViewModel に更新
                                val newTime = LocalTime.of(selectedTime.hour, selectedTime.minute)
                                viewModel.updateExpenseInstanceTime(newTime)
                                isTimePickerVisible=false
                            },
                            onDismiss = {
                                isTimePickerVisible=false
                            },
                            initialDateTime = it
                        )
                    }
                }

            }


            Spacer(modifier=Modifier.padding(8.dp))

            /*************************************************/
            /* 費用の項目 */
            /*************************************************/
            Row(
                modifier=Modifier.fillMaxWidth()
            ) {
                TextField(
                    //数値だけ受け付ける感じにしたい
                    value = viewModel.getExpenseInstanceAmount()?.toString() ?: "",
                    onValueChange ={
                        if(it!="" && it.toLongOrNull()==null){
                            Toast.makeText(
                                context,
                                "数値が大きすぎます。これ以上入力できません" ,
                                Toast.LENGTH_LONG
                            ).show()
                            //viewModel.updateExpenseInstanceAmount(null)
                        }else{
                            viewModel.updateExpenseInstanceAmount(it.toLongOrNull())
                        }
                    },
                    label= {Text(text="Expense")},
                    modifier=Modifier.width(260.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            Spacer(modifier=Modifier.padding(8.dp))

            /*************************************************/
            /* カテゴリーの項目 */
            /*************************************************/
//            Row(
//                modifier=Modifier
//                    .fillMaxWidth()
//            ){
//
//            }

            ExposedDropdownMenuBox(
                expanded=categoryOptionsExpanded,
                onExpandedChange = {
                    categoryOptionsExpanded= !categoryOptionsExpanded
                }
            ) {

                //カテゴリー(選択肢から選んでもらいたい。RoomDB?)
                //@Todo タップしたら画面右からスライドして選択肢が入った列が出てくる感じ
                //とりあえずこれで一応は凌ぐが、本当はもっと使いやすくしたい。
                //カテゴリーの編集画面もほしいし
                /*
                TextField(
                    value=viewModel.getExpenseInstanceCategory():"" ,
                    onValueChange = {
                        viewModel.updateExpenseInstanceCategory(it)
                    },
                    enabled = false,
                    readOnly = true,
                    modifier=Modifier
                        .width(260.dp)
                        .menuAnchor(),//menuAnchorをつけないとだめっぽいな。
                    label={Text(text="Category")},
                    singleLine = true,
                    colors = enabledTextFiledColorSet,
                )


                ExposedDropdownMenu(
                    expanded=categoryOptionsExpanded,
                    onDismissRequest = { categoryOptionsExpanded=false }
                ) {
                    DummyCategory.categoryList.forEachIndexed{
                            index,category->
                        DropdownMenuItem(
                            text = { Text(text = category.name.toString()) },
                            onClick = {
                                viewModel.category.value=category.name
                                categoryOptionsExpanded=false
                            }
                        )
                    }
                }
                 */

            }



            Spacer(modifier=Modifier.padding(8.dp))

            /*************************************************/
            /* Noteの項目 */
            /*************************************************/
            Row(
                modifier=Modifier.fillMaxWidth()
            ){
                //メモ
                TextField(
                    value=viewModel.getInstanceNote()?:"",
                    onValueChange = {
                        viewModel.updateExpenseInstanceNote(it)
                    },
                    modifier=Modifier.width(260.dp),
                    label={Text(text="Note")},
                    singleLine = false
                )
            }

            /*************************************************/
            /* 保存ボタンの実装 */
            /*************************************************/
            Button(
                onClick = {
                    /* きちんと値が入っているかチェック */
                    if(viewModel.getExpenseInstanceAmount() != null){
                        //idがnullなら新規作成ってこと
                        if(viewModel.getExpenseInstanceId()==null){
                            //追加する
                            viewModel.addExpense(viewModel.expense.value)
                            Toast.makeText(
                                context,
                                "追加しました" ,
                                Toast.LENGTH_LONG
                            ).show()
                        } else{//idがnullでなかったら編集
                            //このidのExpenseをupdateする
                            viewModel.updateExpense(viewModel.expense.value)
                            Toast.makeText(
                                context,
                                "更新する" ,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        //リセットする
                        viewModel.resetExpenseInstance()
                        //メイン画面に戻る
                        navController.navigate(Screen.MainScreen.Content.route)
                    }
                    else{
                        /*expenseが入っていないので弾く*/
                        Toast.makeText(
                            context,
                            "Expenseが入力されていません。\n保存できません" ,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                modifier=Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Save")
            }


            if(viewModel.getExpenseInstanceId()==null){
                //新規作成のとき。リセット
                Button(
                    onClick = {
                        //リセット
                        viewModel.resetExpenseInstance()
                    },
                    modifier=Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ){
                    Text("Reset")
                }
            } else{
                //編集
                Button(
                    onClick = {
                        //削除
                        viewModel.removeExpense(viewModel.expense.value)
                        //リセット
                        viewModel.resetExpenseInstance()
                        //元に戻る
                        navController.navigate(Screen.MainScreen.Content.route)
                    },
                    modifier=Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ){
                    Text("Delete")
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialWithDialog(
    onConfirm: (TimePickerState) -> Unit,
    onDismiss: () -> Unit,
    initialDateTime:LocalDateTime//viewModelの値をそのままいれたい
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialDateTime.hour,
        initialMinute = initialDateTime.minute,
        is24Hour = true,
    )

    TimePickerDialog(
        onDismiss = { onDismiss() },
        onConfirm = { onConfirm(timePickerState) }
    ) {
        TimePicker(
            state = timePickerState,
        )
    }
}

@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Dismiss")
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text("OK")
            }
        },
        text = { content() }
    )
}