package gaku.original.myapplication.ui.screens.global.expenseAddEdit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ShoppingBag
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.MainGraph
import gaku.original.myapplication.R
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.ui.common.CategoryDropDown
import gaku.original.myapplication.ui.common.ConfirmAlertDialog
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.ui.common.enabledTextFiledColorSet
import gaku.original.myapplication.ui.common.toYen
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackBarHostState.current

    var isDeleteConfirmDialog by rememberSaveable { mutableStateOf(false) }

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
        onCategoryRefreshClick = {
            viewModel.onCategoryRefreshClick()
        },
        onNoteChange = { index, note ->
            viewModel.onNoteChange(index, note)
        },
        onProductNameChange = { index, productName ->
            viewModel.onProductNameChange(index, productName)
        },
        onPlaceChange = {
            viewModel.onPlaceNameChange(it)
        },
        onSaveClick = {
            viewModel.onSaveClick()
        },
        onDeleteClick = {
            isDeleteConfirmDialog = true
        },
        onAddClick = {
            viewModel.onAddClick()
        }
    )

    if (isDeleteConfirmDialog) {
        ConfirmAlertDialog(
            onDismissRequest = {
                isDeleteConfirmDialog = false
            },
            onClick = {
                viewModel.onDeleteClick()
                isDeleteConfirmDialog = false
            }
        ) {
            Text("Are you sure to Delete?")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseAddEditScreen(
    uiState: ExpenseAddEditUiState,
    snackbarHostState: SnackbarHostState,
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
    onPlaceChange: (String) -> Unit,
    onProductNameChange: (Int, String) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAddClick: () -> Unit
) {

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopBarView(
                title = if (uiState.isEdit) "Edit Expense" else "Add Expense",
                showBackButton = true,
                onBackNavClicked = { onBackNavClick() })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            /*************************************************/
            /* Date and Time */
            /*************************************************/
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextField(
                    value = "${uiState.selectedDate?.format(DateFormatter)}",
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text(text = "Date") },
                    leadingIcon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
                    colors = enabledTextFiledColorSet(),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1.2f)
                        .clickable {
                            onDateFieldClick()
                        },
                )

                TextField(
                    value = "${uiState.selectedTime?.format(TimeFormatter)}",
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text(text = "Time") },
                    leadingIcon = { Icon(Icons.Outlined.Schedule, contentDescription = null) },
                    colors = enabledTextFiledColorSet(),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onTimeFieldClick()
                        },
                )
            }

            if (uiState.isDatePickerVisible) {
                DatePickerModal(
                    onDateSelected = {
                        onDateSelected(it)
                    },
                    onDismiss = onDateDismiss
                )
            }

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

            /*************************************************/
            /* Split input switch */
            /*************************************************/
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSwitchClick() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("分割入力", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Split one payment into several categories",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.isSplitInputEnabled,
                        onCheckedChange = {
                            onSwitchClick()
                        }
                    )
                }
            }

            if (uiState.isSplitInputEnabled) {
                /* show total amount */
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onTotalAmountClick()
                        },
                    value = uiState.totalAmount.toYen(),
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text("Total amount") },
                    textStyle = MaterialTheme.typography.headlineSmall,
                    colors = enabledTextFiledColorSet(),
                )
            }

            uiState.expenseEditList.forEachIndexed { index, item ->
                val isHead = index == 0
                /* In split input, the last item is calculated from the total amount */
                val isCalculated = uiState.isSplitInputEnabled &&
                        !isHead &&
                        index == uiState.expenseEditList.lastIndex

                ExpenseEditItemInput(
                    item = item,
                    index = index,
                    categories = uiState.categories,
                    isHead = isHead,
                    isCalculated = isCalculated,
                    isSplitInputEnabled = uiState.isSplitInputEnabled,
                    onAmountClick = { onAmountClick(index) },
                    onCategorySelected = { onCategorySelected(index, it) },
                    onCategoryEditClick = onCategoryEditClick,
                    onCategoryRefreshClick = onCategoryRefreshClick,
                    onNoteChange = { onNoteChange(index, it) },
                    onProductNameChange = { onProductNameChange(index, it) },
                )

                if (isCalculated) {
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            /* add to array */
                            onAddClick()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "plus",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add item")
                    }
                }
            }

            if (uiState.calculatorTarget != null) {
                /* show Calculator */
                ModalBottomSheet(
                    onDismissRequest = { onCalculatorDismiss() },
                    sheetState = bottomSheetState
                ) {
                    CalculatorUI(
                        initialValue = uiState.calculatorInitialValue,
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
                modifier = Modifier.fillMaxWidth(),
                value = uiState.placeName,
                onValueChange = {
                    onPlaceChange(it)
                },
                label = { Text(text = "Place(空欄可)") },
                leadingIcon = { Icon(Icons.Outlined.Place, contentDescription = null) },
                singleLine = true
            )

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        onClick = {
                            onSaveClick()
                        }
                    ) {
                        Text("Save", style = MaterialTheme.typography.titleMedium)
                    }

                    if (uiState.isEdit) {
                        OutlinedButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            onClick = {
                                /* use popup */
                                onDeleteClick()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

/* One expense in the list. Head item also holds the category edit buttons. */
@Composable
private fun ExpenseEditItemInput(
    item: ExpenseEditItem,
    index: Int,
    categories: List<Category>,
    isHead: Boolean,
    isCalculated: Boolean,
    isSplitInputEnabled: Boolean,
    onAmountClick: () -> Unit,
    onCategorySelected: (Category) -> Unit,
    onCategoryEditClick: () -> Unit,
    onCategoryRefreshClick: () -> Unit,
    onNoteChange: (String) -> Unit,
    onProductNameChange: (String) -> Unit,
) {
    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isSplitInputEnabled) Modifier.padding(12.dp) else Modifier),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isSplitInputEnabled) {
                Text(
                    text = if (isCalculated) "Item ${index + 1} (remaining)" else "Item ${index + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            /*************************************************/
            /* Amount */
            /*************************************************/
            TextField(
                value = item.amount?.toYen() ?: "",
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isCalculated) {
                            Modifier
                        } else {
                            Modifier.clickable {
                                /* open calculator. */
                                onAmountClick()
                            }
                        }
                    ),
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text(text = "Amount") },
                placeholder = { Text("¥0") },
                leadingIcon = { Icon(Icons.Outlined.Calculate, contentDescription = null) },
                textStyle = MaterialTheme.typography.headlineSmall,
                colors = if (isCalculated) TextFieldDefaults.colors() else enabledTextFiledColorSet()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryDropDown(
                    modifier = Modifier.weight(1f),
                    selectedCategory = item.category,
                    categories = categories,
                    onCategorySelected = onCategorySelected
                )

                if (isHead) {
                    // Category Edit Button
                    IconButton(
                        onClick = {
                            onCategoryEditClick()
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_edit_24), // カスタムアイコン
                            contentDescription = "Edit Category",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            onCategoryRefreshClick()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Update",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            /* Note */
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = item.note ?: "",
                onValueChange = onNoteChange,
                label = { Text(text = "Note(空欄可)") },
                leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Notes, contentDescription = null) },
                singleLine = false
            )

            /*****************************************************/
            /* Product Name */
            /*****************************************************/
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = item.productName ?: "",
                onValueChange = onProductNameChange,
                label = { Text(text = "商品名(空欄可)") },
                leadingIcon = { Icon(Icons.Outlined.ShoppingBag, contentDescription = null) },
                singleLine = true
            )
        }
    }

    if (isSplitInputEnabled) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            content()
        }
    } else {
        content()
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
                    category = Category(id = "Food", timestamp = 0L, name = "Food", enabled = true)
                ),
                ExpenseEditItem(
                    amount = 2000,
                    category = Category(id = "Waste", timestamp = 0L, name = "Waste", enabled = true)
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
        onPlaceChange = {},
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
    val digitColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val onDigitColor = MaterialTheme.colorScheme.onSurface
    val operatorColor = MaterialTheme.colorScheme.secondaryContainer
    val onOperatorColor = MaterialTheme.colorScheme.onSecondaryContainer
    val clearColor = MaterialTheme.colorScheme.tertiaryContainer
    val onClearColor = MaterialTheme.colorScheme.onTertiaryContainer

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padDp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Text(
                text = input.ifEmpty { "0" },
                style = MaterialTheme.typography.displaySmall,
                color = if (input.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                textAlign = TextAlign.End
            )
        }
        /* なんだかんだベタ打ちがやりやすい、、 */
        Row(modifier = Modifier.fillMaxWidth()) {
            CalculatorButton(
                label = "C",
                initialInput = input,
                onInputChanged = { input = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(padDp),
                containerColor = clearColor,
                contentColor = onClearColor
            )
            CalculatorButton(
                label = "DEL",
                initialInput = input,
                onInputChanged = { input = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(padDp),
                containerColor = clearColor,
                contentColor = onClearColor
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
                    .weight(2f)
                    .padding(padDp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
        listOf(
            listOf("7", "8", "9") to "÷",
            listOf("4", "5", "6") to "×",
            listOf("1", "2", "3") to "-",
            listOf("", "0", ".") to "+",
        ).forEach { (digits, operator) ->
            Row(modifier = Modifier.fillMaxWidth()) {
                digits.forEach { label ->
                    if (label.isEmpty()) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        CalculatorButton(
                            label = label,
                            initialInput = input,
                            onInputChanged = { input = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(padDp),
                            containerColor = digitColor,
                            contentColor = onDigitColor
                        )
                    }
                }
                CalculatorButton(
                    label = operator,
                    initialInput = input,
                    onInputChanged = { input = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(padDp),
                    containerColor = operatorColor,
                    contentColor = onOperatorColor
                )
            }
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
        modifier = modifier.height(60.dp),
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(label, fontSize = 22.sp)
    }
}