package gaku.original.myapplication.ui.view.main

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import gaku.original.myapplication.R
import gaku.original.myapplication.Screen
import gaku.original.myapplication.Utility.LogAkitaDebug
import gaku.original.myapplication.Utility.evalExpression
import gaku.original.myapplication.Utility.roundToLongOrNull
import gaku.original.myapplication.Utility.toLocalDateTime
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.ui.common.enabledTextFiledColorSet
import gaku.original.myapplication.ui.view.BottomBarView
import gaku.original.myapplication.ui.view.TopBarView
import gaku.original.myapplication.viewModel.ExpenseAddEditViewModel
import kotlinx.coroutines.launch
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

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseAddEditView(
    viewModel: ExpenseAddEditViewModel = hiltViewModel(),
    navController: NavController
) {
    val viewName = "ExpenseAddEditView"

    //Toastとか用
    val context = LocalContext.current

    //日付、時間の選択肢用
    var isDatePickerVisible by remember { mutableStateOf(false) }
    var isTimePickerVisible by remember { mutableStateOf(false) }

    //計算機用
    var showCalculator by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    /* StateFlowあたりよくわかっていない、、ちゃんと勉強しないと */
    val allCategories by remember { viewModel.allCategories }.collectAsState(initial = emptyList())

    var categoryOptionsExpanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackBarHostState = remember {
        SnackbarHostState()
    }

    Scaffold(
        topBar = {
            //悩みどころだが、BackだとGraphから来たときにGraphに戻る可能性があるので
            //強制的にMainScreenに行くことにする。しっかり設計しないとヒューマンエラー起きそうだな
            TopBarView(
                title = "What is essential is invisible to the eye",
                onBackNavClicked = {
//                    navController.navigate(Screen.MainScreen.Content.route)
                    navController.popBackStack()
                },
                showBackButton = true
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        bottomBar = { BottomBarView(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center
        ) {
            val dateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd")
            val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

            /*************************************************/
            /* 日付の項目 */
            /*************************************************/
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Absolute.Left
            ) {
                TextField(
                    value = toLocalDateTime(viewModel.currentTmpExpense.datetime)?.format(dateFormat)
                        ?: "日付が入っていません",
                    onValueChange = {},
                    enabled = false,
                    readOnly = true,
                    label = { Text(text = "Date") },
                    modifier = Modifier
                        .width(150.dp)
                        .clickable {
                            isDatePickerVisible = true
                        },
                    colors = enabledTextFiledColorSet()
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
                                viewModel.updateTmpExpenseDate(selectedDate)
                            }
                        },
                        onDismiss = { isDatePickerVisible = false }
                    )
                }

                Spacer(modifier = Modifier.padding(8.dp))

                TextField(
                    value = toLocalDateTime(viewModel.currentTmpExpense.datetime)?.format(timeFormat)
                        ?: "時間が入っていません",
                    onValueChange = {},
                    enabled = false,
                    readOnly = true,
                    label = { Text(text = "Time") },
                    modifier = Modifier
                        .width(100.dp)
                        .clickable {
                            isTimePickerVisible = true
                        },
                    colors = enabledTextFiledColorSet()
                )

                //時間をタップしたらダイアログを表示して選択させる
                //Clickableの中身はComposable関数を入れられないらしい？だからここで分けて書いている
                if (isTimePickerVisible) {
                    //nullでないときのみ時刻を表示
                    viewModel.currentTmpExpense.datetime?.let {
                        DialWithDialog(
                            onConfirm = { selectedTime ->
                                // 選択した時間を取得して ViewModel に更新
                                val newTime = LocalTime.of(selectedTime.hour, selectedTime.minute)
                                viewModel.updateTmpExpenseTime(newTime)
                                isTimePickerVisible = false
                            },
                            onDismiss = {
                                isTimePickerVisible = false
                            },
                            //@HACK let内に入っているからnullなわけないけど一応気をつけて
                            initialDateTime = toLocalDateTime(it)!!
                        )
                    }
                }

            }


            Spacer(modifier = Modifier.padding(8.dp))

            /*************************************************/
            /* 費用の項目 */
            /*************************************************/
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    //数値だけ受け付ける感じにしたい
                    value = viewModel.currentTmpExpense.amount?.toString() ?: "",
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text(text = "Amount") },
                    modifier = Modifier
                        .width(260.dp)
                        .clickable {
                            showCalculator = true
                            Log.d(viewName, "Amount was tapped!!!")
                        },
                    colors = enabledTextFiledColorSet()
                )
            }

            if (showCalculator) {
                ModalBottomSheet(
                    onDismissRequest = { showCalculator = false },
                    sheetState = bottomSheetState
                ) {
                    CalculatorUI(
                        onDecide = {/* 日本円を使っている限りは、整数に変換。おいおい外貨にも対応 */
                            if (true/* 日本円。設定から制御できるように、、 */) {
                                val convertedVal = it.roundToLongOrNull()/* 自作 */
                                if (it != "" && convertedVal == null) {
                                    scope.launch {
                                        snackBarHostState.showSnackbar("数値が大きすぎます。これ以上入力できません")
                                    }
                                    //viewModel.updateExpenseInstanceAmount(null)
                                } else {
                                    viewModel.updateTmpExpenseAmount(convertedVal)
                                    showCalculator = false
                                }
                            } else {
                                /* ラウンドしない場合、、 */
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.padding(8.dp))

            /*************************************************/
            /* カテゴリーの項目 */
            /*************************************************/

            Row()
            {
                ExposedDropdownMenuBox(
                    expanded = categoryOptionsExpanded,
                    onExpandedChange = {
                        if (categoryOptionsExpanded == false) {
                            //@TODO ここうまく機能していない。
                            if (allCategories.isEmpty()) {
                                viewModel.updateStoredCategories { statusInfo ->
                                    if (statusInfo.status == SuspendFuncStatus.SUCCESS) {
                                        if (allCategories.isEmpty()) {
                                            //何もなかったらToastを出す
                                            LogAkitaDebug("Properly fetched categories")
                                            scope.launch {//なんかここエラー出るぞ？
                                                snackBarHostState.showSnackbar(
                                                    "カテゴリーが何も登録されていません。\n編集ボタンから追加してください",
                                                    actionLabel = "OK",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        } else {
                                            categoryOptionsExpanded = true
                                        }
                                    } else {
                                        LogAkitaDebug("Fail msg:${statusInfo.errorMessage}")
                                        scope.launch {
                                            snackBarHostState.showSnackbar(
                                                "カテゴリーが取得できていません。",
                                                actionLabel = "OK",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                        categoryOptionsExpanded = false
                                    }
                                }
                            } else {
                                /* カテゴリーがちゃんと入っていればtrueになる */
                                categoryOptionsExpanded = true
                            }
                        } else {
                            categoryOptionsExpanded = false
                        }
                    }
                ) {

                    //カテゴリー(選択肢から選んでもらいたい。RoomDB?)
                    //@Todo タップしたら画面右からスライドして選択肢が入った列が出てくる感じ
                    //とりあえずこれで一応は凌ぐが、本当はもっと使いやすくしたい。
                    //カテゴリーの編集画面もほしいし
                    TextField(
                        value = viewModel.currentTmpExpense.category?.name ?: "",
                        onValueChange = {/* ドロップダウンから選択すれば値が更新される */ },
                        enabled = false,
                        readOnly = true,
                        modifier = Modifier
                            .width(260.dp)
                            .menuAnchor(),//menuAnchorをつけないとだめっぽいな。
                        label = { Text(text = "Category") },
                        singleLine = true,
                        colors = enabledTextFiledColorSet(),
                    )

                    ExposedDropdownMenu(
                        expanded = categoryOptionsExpanded,
                        onDismissRequest = { categoryOptionsExpanded = false }
                    ) {
                        LogAkitaDebug("Allcategories???=${allCategories}")
                        allCategories.forEachIndexed { _, category ->
                            DropdownMenuItem(
                                text = { Text(text = category.name.toString()) },
                                onClick = {
                                    viewModel.updateTmpExpenseCategory(category)
                                    categoryOptionsExpanded = false
                                }
                            )
                        }
                    }

                }

                // カテゴリー編集ボタン
                IconButton(
                    onClick = {
                        navController.navigate(Screen.MainScreen.CategoryAddEdit.route)
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_edit_24), // カスタムアイコン
                        contentDescription = "Edit Category"
                    )
                }

                IconButton(
                    onClick = {
                        viewModel.updateStoredCategories {
                            /* カテゴリーを更新する */
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Update"
                    )
                }
            }

            Spacer(modifier = Modifier.padding(8.dp))

            /*************************************************/
            /* Noteの項目 */
            /*************************************************/
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                //メモ
                TextField(
                    value = viewModel.currentTmpExpense.note ?: "",
                    onValueChange = {
                        viewModel.updateTmpExpenseNote(it)
                    },
                    modifier = Modifier.width(260.dp),
                    label = { Text(text = "Note") },
                    singleLine = false
                )
            }

            /*************************************************/
            /* 保存ボタンの実装 */
            /*************************************************/
            Button(
                onClick = {
                    /* きちんと値が入っているかチェック */
                    if (viewModel.currentTmpExpense.amount == null) {
                        /*amount入っていないので弾く*/
                        scope.launch {
                            snackBarHostState.showSnackbar("金額が入力されていません。\n保存できません")
                        }
                    } else if (viewModel.currentTmpExpense.category == null) {
                        scope.launch {
                            snackBarHostState.showSnackbar("Categoryが選択されていません。\n保存できません")
                        }
                    } else {
                        //idがnullなら新規作成ってこと
                        if (viewModel.currentTmpExpense.id == null) {
                            //追加する
                            //引数はないけど(詳しくはメソッドの中身見て)、この時点でのTmpExpenseを追加する
                            viewModel.addTmpExpenseToDb(
                                onStart = {/* 追加します的な,, */ },
                                callback = { status ->
                                    if (status.status == SuspendFuncStatus.SUCCESS) {
                                        /**/
                                    } else if (status.status == SuspendFuncStatus.TIMEOUT) {
                                        /**/
                                    } else if (status.status == SuspendFuncStatus.FAILED) {
                                        /*  */
                                    } else {
                                        /* よくわからんstatus */
                                    }
                                })
                            scope.launch {
                                snackBarHostState.showSnackbar("追加しました")
                            }
                        } else {//idがnullでなかったら編集
                            //このidのExpenseをupdateする
                            viewModel.updateTmpExpenseToDb(
                                onStart = {/* 追加しますてきな？？ */ },
                                callback = {/* 失敗したときの対応 */ })//@TODO 空だけど、あとで整備
                            scope.launch {
                                snackBarHostState.showSnackbar("更新する")
                            }
                        }
                        //リセットする
                        viewModel.resetTmpExpense()
                        //メイン画面に戻る
                        navController.navigate(Screen.MainScreen.Content.route)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Save")
            }


            if (viewModel.currentTmpExpense.id == null) {
                //新規作成のとき。リセット
                Button(
                    onClick = {
                        //リセット
                        viewModel.resetTmpExpense()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text("Reset")
                }
            } else {
                //編集
                Button(
                    onClick = {
                        //削除
                        viewModel.removeTmpExpenseToDb(
                            onStart = {},
                            callback = {/* 失敗したときの処理 */ })
                        //リセット
                        viewModel.resetTmpExpense()
                        //元に戻る
                        navController.navigate(Screen.MainScreen.Content.route)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
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
    initialDateTime: LocalDateTime//viewModelの値をそのままいれたい
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

@Composable
fun CalculatorUI(
    onDecide: (String) -> Unit = {}/* 決定ボタンを作ろうと思ったが、現状無理 */
) {
    var input by remember { mutableStateOf("") }

    val hasOperator = input.contains(Regex("[+\\-×÷]"))

    val padDp = 4.dp
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = input,
            fontSize = 32.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            textAlign = TextAlign.End
        )
        /* なんだかんだベタ打ちがやりやすい、、 */
        Row(modifier = Modifier.fillMaxWidth()) {
            CalculatorButton(
                label = "C",
                initialInput = input,
                onInputChanged = { input = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(padDp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
            CalculatorButton(
                label = "DEL",
                initialInput = input,
                onInputChanged = { input = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(padDp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )

            CalculatorButton(
                label = if (hasOperator) "=" else "決定",
                initialInput = input,
                onEqual = {
                    /* onEqualはCalculatorUIにわたす必要はない。 */
                    /* 決定ボタンがないから決定ボタン代わり */
                    input = it
                    LogAkitaDebug("input計算されてるか？${input}")
                    onDecide(it/* =input */)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(padDp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {

            listOf("7", "8", "9").forEach { label ->
                CalculatorButton(
                    label = label,
                    initialInput = input,
                    onInputChanged = { input = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(padDp)
                )
            }
            CalculatorButton(
                label = "÷",
                initialInput = input,
                onInputChanged = { input = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(padDp),
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("4", "5", "6").forEach { label ->
                CalculatorButton(
                    label = label,
                    initialInput = input,
                    onInputChanged = { input = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(padDp)
                )
            }
            CalculatorButton(
                label = "×",
                initialInput = input,
                onInputChanged = { input = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(padDp),
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("1", "2", "3").forEach { label ->
                CalculatorButton(
                    label = label,
                    initialInput = input,
                    onInputChanged = { input = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(padDp)
                )
            }
            CalculatorButton(
                label = "-",
                initialInput = input,
                onInputChanged = { input = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(padDp),
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.weight(1f))
            CalculatorButton(
                label = "0",
                initialInput = input,
                onInputChanged = { input = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(padDp)
            )
            CalculatorButton(
                label = ".",
                initialInput = input,
                onInputChanged = { input = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(padDp)
            )
            CalculatorButton(
                label = "+",
                initialInput = input,
                onInputChanged = { input = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(padDp),
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}


/* callbackは呼び出し側で設定する */
@Composable
fun CalculatorButton(
    modifier: Modifier = Modifier,
    label: String,
    initialInput: String,
    onInputChanged: (String) -> Unit = {},
    onEqual: (String) -> Unit = {},/* =ボタンを押した時(今のところ決定ボタンと同じ挙動にする。) */
    /* 将来的にBasicTextFieldでカーソル移動もできるようにする。 */

    onDecide: (String) -> Unit = {},/* 決定ボタンを押した時 */
    containerColor: Color = MaterialTheme.colorScheme.tertiary,
    contentColor: Color = MaterialTheme.colorScheme.onTertiary
) {
    Button(
        onClick = {

            var updatedInput = initialInput
            when (label) {
                "C" -> {
                    updatedInput = ""
                    onInputChanged(updatedInput)
                }

                "DEL" -> {
                    updatedInput = updatedInput.dropLast(1)
                    onInputChanged(updatedInput)
                }

                "=", "決定" -> {
                    val lastChar = updatedInput.lastOrNull()
                    val lastIsOperator = lastChar in listOf('+', '-', '×', '÷')
                    if (updatedInput == "") {
                        /* ""のまま渡してもエラーが出る */
                    } else if (updatedInput == ".") {
                        /* ただの小数点だとエラーが出る */
                    } else if (lastIsOperator) {
                        /* 最後の値が演算子になっている */
                    } else {
                        updatedInput = evalExpression(updatedInput).toString()
                        onEqual(updatedInput)
                    }
                }

                else -> {
                    val lastChar = updatedInput.lastOrNull()
                    val isOperator = label in listOf("+", "-", "×", "÷")
                    val lastIsOperator = lastChar in listOf('+', '-', '×', '÷')

                    updatedInput = if (isOperator && lastIsOperator) {
                        /* 演算子が続いたときは返す */
                        updatedInput.dropLast(1) + label
                    } else if (label == "." && (lastChar == '.' || lastIsOperator)) {
                        /* 小数点のあとに小数点押しても反応させない */
                        /* 演算子のあとに小数点押しても反応させない */
                        updatedInput
                    } else if (label == "." && initialInput == "") {
                        updatedInput
                    } else {
                        updatedInput + label
                    }
                    onInputChanged(updatedInput)
                }
            }
        },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
    ) {
        Text(label, fontSize = 20.sp)
    }
}