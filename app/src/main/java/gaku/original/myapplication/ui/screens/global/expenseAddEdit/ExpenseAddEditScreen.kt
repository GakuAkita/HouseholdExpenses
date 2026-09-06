package gaku.original.myapplication.ui.screens.global.expenseAddEdit

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.MainGraph
import gaku.original.myapplication.R
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.ui.common.CategoryDropDown
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.ui.common.enabledTextFiledColorSet
import gaku.original.myapplication.utility.LogAkitaDebug
import gaku.original.myapplication.utility.evalExpression
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/*
・まずはすべて手入力で実装する
FloatingActionボタンから来た場合は、ボタンを叩いた時間を入力
カレンダーの日付を叩いてきたときはその日付と時間(今の時間)をデフォルトでいれる
 */
enum class FromScreen {
    SEARCH,
    MAIN_CONTENT,
    UNKNOWN
}

private val DateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
private val TimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun ExpenseAddEditScreenRoot(
    viewModel: ExpenseAddEditViewModel,
    navHostController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = LocalSnackBarHostState.current

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it, actionLabel = "OK")
            viewModel.onMessageShown()
        }
    }

    LaunchedEffect(uiState.isSaveDone) {
        if (uiState.isSaveDone) {
            navHostController.popBackStack()
            //snackbarHostState.showSnackbar("Save success!")
        }
    }

    LaunchedEffect(uiState.isDeleteDone) {
        if (uiState.isDeleteDone) {
            navHostController.popBackStack()
            //snackbarHostState.showSnackbar("Deleted!")
        }
    }

    ExpenseAddEditScreen(
        uiState,
        snackbarHostState,
        totalAmountIndex = viewModel.totalAmountIndex,
        onBackNavClick = {
            navHostController.popBackStack()
        },
        onDateFieldClick = {
            viewModel.onDateFieldClick()
        },
        onDateDismiss = {
            viewModel.onDatePickerDismiss()
        },
        onDateSelected = {
            viewModel.onDateSelected(it)
        },
        onTimeFieldClick = {
            viewModel.onTimeFieldClick()
        },
        onTimePickerDismiss = {
            viewModel.onTimePickerDismiss()
        },
        onTimeSelected = {
            viewModel.onTimeSelected(it)
        },
        onSwitchClick = {
            viewModel.onSwitchClick()
        },
        onTotalAmountClick = {
            viewModel.onTotalAmountClick()
        },
        onAmountClick = {
            viewModel.onAmountClick(it)
        },
        onCalculatorDecide = {
            viewModel.onCalculatorDecide(it)
        },
        onCalculatorDismiss = {
            viewModel.onCalculatorDismiss()
        },
        onCategorySelected = { index, category ->
            viewModel.onCategorySelected(index, category)
        },
        onCategoryEditClick = {
            navHostController.navigate(MainGraph.Global.CategoryAddEdit)
        },
        onCategoryRefreshClick = {},
        onNoteChange = { index, note ->
            viewModel.onNoteChange(index, note)
        },
        onProductNameChange = { index, productName ->
            viewModel.onProductNameChange(index, productName)
        },
        onSaveClick = {
            viewModel.onSaveClick()
        },
        onDeleteClick = {
            viewModel.onDeleteClick()
        },
        onAddClick = {
            viewModel.onAddClick()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseAddEditScreen(
    uiState: ExpenseAddEditUiState,
    snackbarHostState: SnackbarHostState,
    totalAmountIndex: Int = -1,
    onBackNavClick: () -> Unit,
    onDateFieldClick: () -> Unit,
    onDateDismiss: () -> Unit,
    onDateSelected: (Long?) -> Unit,
    onTimeFieldClick: () -> Unit,
    onTimePickerDismiss: () -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    onSwitchClick: () -> Unit,
    onTotalAmountClick: () -> Unit,
    onAmountClick: (Int) -> Unit,
    onCalculatorDecide: (String) -> Unit,
    onCalculatorDismiss: () -> Unit,
    onCategorySelected: (Int, Category) -> Unit,
    onCategoryEditClick: () -> Unit,
    onCategoryRefreshClick: () -> Unit,
    onNoteChange: (Int, String) -> Unit,
    onProductNameChange: (Int, String) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAddClick: () -> Unit
) {

    val basicModifier = remember { Modifier.width(260.dp) }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopBarView(
                title = if (uiState.isEdit) "Edit" else "Add",
                showBackButton = true,
                onBackNavClicked = { onBackNavClick() })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {

            /*************************************************/
            /* Date and Time */
            /*************************************************/
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                TextField(
                    value = "${uiState.selectedDate?.format(DateFormatter)}",
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text(text = "Date") },
                    colors = enabledTextFiledColorSet(),
                    modifier = Modifier
                        .width(150.dp)
                        .clickable {
                            onDateFieldClick()
                        },
                )

                if (uiState.isDatePickerVisible) {
                    DatePickerModal(
                        onDateSelected = {
                            onDateSelected(it)
                        },
                        onDismiss = onDateDismiss
                    )
                }
                Spacer(modifier = Modifier.padding(8.dp))

                TextField(
                    value = "${uiState.selectedTime?.format(TimeFormatter)}",
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text(text = "Time") },
                    colors = enabledTextFiledColorSet(),
                    modifier = Modifier
                        .width(150.dp)
                        .clickable {
                            onTimeFieldClick()
                        },
                )

                if (uiState.isTimePickerVisible) {
                    DialWithDialog(
                        onConfirm = {
                            onTimeSelected(LocalTime.of(it.hour, it.minute))
                        },
                        onDismiss = {
                            onTimePickerDismiss()
                        },
                        initialTime = uiState.selectedTime
                            ?: LocalTime.now()/* selectedTime is basically not null */
                    )
                }
            }

            if (uiState.isSplitInputEnabled) {
                /* show total amount */
                TextField(
                    modifier = basicModifier
                        .clickable {
                            onTotalAmountClick()
                        }
                        .padding(horizontal = 2.dp, vertical = 4.dp),
                    value = uiState.totalAmount.toString(),
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text("Total amount") },
                    colors = enabledTextFiledColorSet(),
                )
            }

            uiState.expenseEditList.forEachIndexed { index, item ->
                val isLastElement =
                    index == uiState.expenseEditList.size - 1 &&
                            uiState.isSplitInputEnabled &&
                            index != 0
                Column(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .then(
                            if (uiState.isSplitInputEnabled) {
                                Modifier.border(
                                    1.dp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            } else {
                                Modifier
                            }
                        )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        /*************************************************/
                        /* Amount */
                        /*************************************************/
                        TextField(
                            value = "${item.amount ?: ""}",
                            modifier = basicModifier.then(
                                if (isLastElement) {
                                    Modifier
                                } else {
                                    Modifier.clickable {
                                        /* open calculator. */
                                        onAmountClick(index)
                                    }
                                }.padding(4.dp)
                            ),
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text(text = "Amount") },
                            colors = if (isLastElement) TextFieldDefaults.colors() else enabledTextFiledColorSet()
                        )

                        if (index == 0) {
                            Switch(
                                checked = uiState.isSplitInputEnabled,
                                onCheckedChange = {
                                    onSwitchClick()
                                },
                                modifier = Modifier.padding(vertical = 0.dp)
                            )
                            Text("分割入力")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CategoryDropDown(
                            modifier = basicModifier.padding(4.dp),
                            selectedCategory = uiState.expenseEditList[index].category,
                            categories = uiState.categories,
                            onCategorySelected = {
                                onCategorySelected(index, it)
                            }
                        )

                        if (index == 0) {
                            // Category Edit Button
                            IconButton(
                                onClick = {
                                    onCategoryEditClick()
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_edit_24), // カスタムアイコン
                                    contentDescription = "Edit Category"
                                )
                            }

                            IconButton(
                                onClick = {
                                    onCategoryRefreshClick()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Update"
                                )
                            }

                        }
                    }

                    /* Note */
                    TextField(
                        modifier = basicModifier.padding(4.dp),
                        value = item.note ?: "",
                        onValueChange = {
                            onNoteChange(index, it)
                        },
                        label = { Text(text = "Note(空欄可)") },
                        singleLine = false
                    )

                    /*****************************************************/
                    /* Product Name */
                    /*****************************************************/
                    TextField(
                        modifier = basicModifier.padding(4.dp),
                        value = item.productName ?: "",
                        onValueChange = {
                            onProductNameChange(index, it)
                        },
                        label = { Text(text = "商品名(空欄可)") },
                    )
                }

                if (isLastElement) {
                    Row(
                        modifier = basicModifier,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            onClick = {
                                /* add to array */
                                onAddClick()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "plus"
                            )
                        }
                    }
                }

                /* Calculator can be here */
                /* uiState.isShowCalculator && selectedIndex == index */
            }

            if (uiState.isShowCalculator) {
                /* show Calculator */
                ModalBottomSheet(
                    onDismissRequest = { onCalculatorDismiss() },
                    sheetState = bottomSheetState
                ) {
                    CalculatorUI(
                        initialValue = if (uiState.selectedIndex == totalAmountIndex) uiState.totalAmount else uiState.expenseEditList[uiState.selectedIndex!!].amount
                            ?: 0L,
                        onDecide = {/* 日本円を使っている限りは、整数に変換。おいおい外貨にも対応 */
                            onCalculatorDecide(it)
                        }
                    )
                }
            }

            /*****************************************************/
            /* Place Name */
            /*****************************************************/
            TextField(
                modifier = basicModifier.padding(horizontal = 4.dp, vertical = 4.dp),
                value = uiState.placeName,
                onValueChange = {},
                label = { Text(text = "Place(空欄可)") },
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Column {
                    Button(
                        modifier = Modifier
                            .widthIn(max = 300.dp)
                            .fillMaxWidth()
                            .padding(8.dp),
                        onClick = {
                            onSaveClick()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Save")
                    }

                    if (uiState.isEdit) {
                        Button(
                            modifier = Modifier
                                .widthIn(max = 140.dp)
                                .padding(8.dp),
                            onClick = {
                                /* use popup */
                                onDeleteClick()
                            },
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
    }
}

@Preview(showBackground = true)
@Composable
fun ExpenseAddEditScreenPreview() {
    ExpenseAddEditScreen(
        uiState = ExpenseAddEditUiState(
            selectedDate = LocalDate.now(),
            selectedTime = LocalTime.now(),
            expenseEditList = listOf(
                ExpenseEditItem(
                    amount = 1000,
                    category = Category(name = "Food")
                ),
                ExpenseEditItem(
                    amount = 2000,
                    category = Category(name = "Waste")
                )
            ),
            isSplitInputEnabled = false,
            isLoading = false,
            isEdit = true
        ),
        snackbarHostState = SnackbarHostState(),
        onBackNavClick = {},
        onDateFieldClick = {},
        onDateSelected = {},
        onDateDismiss = {},
        onTimeFieldClick = {},
        onTimePickerDismiss = {},
        onTimeSelected = {},
        onSwitchClick = {},
        onTotalAmountClick = {},
        onAmountClick = {},
        onCalculatorDecide = {},
        onCalculatorDismiss = {},
        onCategorySelected = { _, _ -> },
        onCategoryEditClick = {},
        onCategoryRefreshClick = {},
        onNoteChange = { _, _ -> },
        onProductNameChange = { _, _ -> },
        onSaveClick = {},
        onDeleteClick = {},
        onAddClick = {}
    )
}

@Composable
fun RowSpace() {
    Spacer(modifier = Modifier.padding(8.dp))
}

@Composable
fun CategoryAssignmentText() {
    val fontSize = 10.sp
    Text("自動カテゴリー割当登録", fontSize = fontSize, lineHeight = fontSize)
}

@Composable
fun CategoryAssignmentArea(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.clickable {
            onClick()
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            onClick()
        }) {
            Icon(
                painter = painterResource(id = R.drawable.docs_add_on),
                contentDescription = "add_on"
            )
        }
        CategoryAssignmentText()
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
    initialTime: LocalTime//viewModelの値をそのままいれたい
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
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
    initialValue: Long = 0L,
    onDecide: (String) -> Unit = {}/* 決定ボタンを作ろうと思ったが、現状無理 */
) {
    var input by rememberSaveable { mutableStateOf(if (initialValue == 0L) "" else initialValue.toString()) }

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