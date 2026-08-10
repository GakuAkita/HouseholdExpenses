package gaku.original.myapplication.ui.screens.global.expenseAddEdit

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.R
import gaku.original.myapplication.data.dataClass.Category
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
            snackbarHostState.showSnackbar(it)
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
        }
    )
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
    onSwitchClick: ()->Unit
) {

    val basicModifier = remember { Modifier.width(260.dp) }
    Scaffold(
        topBar = {
            TopBarView(
                title = "Add/Edit",
                showBackButton = true,
                onBackNavClicked = { onBackNavClick() })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
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
//                TextField(
//
//                )
            }

            uiState.expenseEditList.forEachIndexed { index, item ->
                val isLastElement =
                    index == uiState.expenseEditList.size - 1 &&
                            uiState.isSplitInputEnabled &&
                            index != 0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = "${item.amount ?: ""}",
                        modifier = basicModifier.then(
                            if (isLastElement) {
                                Modifier
                            } else {
                                Modifier.clickable {
                                    /* open calculator. */
                                }
                            }
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

                TextField(
                    value = item.category?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text(text = "Category") },
                    modifier = basicModifier.clickable{

                    },
                    colors = enabledTextFiledColorSet()
                )
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
                    category = Category(name="Food")
                ),
                ExpenseEditItem(
                    amount = 2000,
                    category = Category(name="Waste")
                )
            ),
            isSplitInputEnabled = true
        ),
        snackbarHostState = SnackbarHostState(),
        onBackNavClick = {},
        onDateFieldClick = {},
        onDateSelected = {},
        onDateDismiss = {},
        onTimeFieldClick = {},
        onTimePickerDismiss = {},
        onTimeSelected = {},
        onSwitchClick = {}
    )
}

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun _ExpenseAddEditView(
    viewModel: ExpenseAddEditViewModel = hiltViewModel(),
    navController: NavController,
    from: String/* 遷移元のスクリーン */
) {
//    val fromScreen = when (from) {
//        // @Todo 作り直し
////        Screen.SearchScreen.route -> FromScreen.SEARCH
////        Screen.MainScreen.Content.route -> FromScreen.MAIN_CONTENT
//        else -> FromScreen.UNKNOWN
//    }
//
//    val context = LocalContext.current
//
//    val viewName = "ExpenseAddEditView"
//
//    var selectedDate by remember { mutableStateOf<LocalDate?>(viewModel.getTimeZoneDate()) }
//    var selectedTime by remember { mutableStateOf<LocalTime?>(viewModel.getTimeZoneTime()) }
//
//    //日付、時間の選択肢用
//    var isDatePickerVisible by remember { mutableStateOf(false) }
//    var isTimePickerVisible by remember { mutableStateOf(false) }
//
//    //計算機用
//    var showCalculator by remember { mutableStateOf(false) }
//    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
//
//    /* StateFlowあたりよくわかっていない、、ちゃんと勉強しないと */
//    val allCategories by viewModel.allCategories.collectAsState(initial = emptyList())
//    val expenseList by viewModel.expenseList.collectAsState()
//
//    var categoryOptionsExpanded by remember { mutableStateOf(false) }
//
//    /* 分割入力ボタン */
//    val splitInputState by viewModel.splitInputEnabled.collectAsState()
//    val totalAmount by viewModel.totalAmount.collectAsState()
//
//    /* 割当をするかしないか問題 */
//    var showCategoryAssignmentDialog by rememberSaveable { mutableStateOf(false) }
//    var showDeleteResetConfirmDialog by remember { mutableStateOf(false) }
//    var namePattern by remember { mutableStateOf(CategoryAssignNamePattern.STORE) }
//    var assignmentEdited by remember { mutableStateOf(CategoryAssignment()) }
//
//    /* 保存時、ボタンを押せないようにするため */
//    val loadingState by viewModel.loadingState.collectAsState()
//
//    val scope = rememberCoroutineScope()
//    val snackBarHostState = remember {
//        SnackbarHostState()
//    }
//
//    /**
//     * ViewModel側に移動予定!
//     */
//    fun handleSaveClick() {
//        if (selectedDate == null || selectedTime == null) {
//            scope.launch {
//                snackBarHostState.showSnackbar("日付と時間が選択されていません", actionLabel = "OK")
//            }
//            return
//        }
//
//        /**
//         * 0はいいけど、負の値があったら弾く
//         */
//        for (expense in viewModel.expenseList.value) {
//            if (expense.amount == null) {
//                scope.launch {
//                    snackBarHostState.currentSnackbarData?.dismiss()
//                    snackBarHostState.showSnackbar("金額が入力されていません", actionLabel = "OK")
//                }
//                return
//            } else if (expense.amount!! < 0) {/* 落ちるかも？？ */
//                scope.launch {
//                    snackBarHostState.currentSnackbarData?.dismiss()
//                    snackBarHostState.showSnackbar(
//                        "負の値になっている金額ものがあります",
//                        actionLabel = "OK"
//                    )
//                }
//                return
//            }
//        }
//
//        // カテゴリーチェック（ローカルDBキャッシュがあるので必須化）
//        for (expense in viewModel.expenseList.value) {
//            if (expense.category == null && !BuildConfig.DEBUG) {
//                scope.launch {
//                    snackBarHostState.currentSnackbarData?.dismiss()
//                    snackBarHostState.showSnackbar(
//                        "カテゴリーを選択してください",
//                        actionLabel = "OK"
//                    )
//                }
//                return
//            }
//        }
//
//        if (viewModel.calcExpenseListSum() != totalAmount && splitInputState) {
//            scope.launch {
//                snackBarHostState.currentSnackbarData?.dismiss()
//                snackBarHostState.showSnackbar("それぞれの費用の合計と合計金額が一致しません")
//            }
//            return
//        }
//
//        val dateTime = LocalDateTime.of(selectedDate, selectedTime)
//        val isoStr = AppTimeZone.localDateTimeToIsoString(dateTime)
//        if (isoStr == null) {
//            scope.launch {
//                snackBarHostState.showSnackbar(
//                    "日付と時間の変換に失敗しました\n管理者に連絡してください",
//                    actionLabel = "OK"
//                )
//            }
//        }
////        LogAkitaDebug("converted String ${isoStr}")
//        /* isoStrがnullであることはない。上でチェックしている。 */
//        viewModel.updateExpenseDatetime(isoStr!!)
//
//        if (viewModel.getHeadExpense().id == null) {
//            viewModel.addExpenseToDb(callback = {
//                /* 成功のときのみ */
//                if (it.status == FuncStatus.SUCCESS) {
//                    scope.launch {
//                        snackBarHostState.showSnackbar("追加しました")
//                    }
//                    navController.popBackStack()
//                } else {
//                    viewModel.setLoadingState(false)
//                    scope.launch {
//                        snackBarHostState.showSnackbar("更新に失敗しました:${it.errorMessage}")
//                    }
//                }
//            })
//        } else {
//            viewModel.updateExpenseToDb(onStart = {}, callback = {
//                if (it.status == FuncStatus.SUCCESS) {
//                    scope.launch {
//                        snackBarHostState.showSnackbar("更新する")
//                    }
//                    navController.popBackStack()
//                } else {
//                    viewModel.setLoadingState(false)
//                    scope.launch {
//                        snackBarHostState.showSnackbar("更新に失敗しました:${it.errorMessage}")
//                    }
//                }
//            })
//        }
//    }
//
//    val typeList = viewModel.getSeparatedGeneratedType()
//    val mainType = typeList.getOrNull(0)
//    val subType = typeList.getOrNull(1)
//    /* デバッグ用 */
//    LaunchedEffect(allCategories) {
//        viewModel.getTimeZoneDate()
//        Log.d("UI", "Categories updated in UI: $allCategories")
//    }
//
//    Scaffold(
//        modifier = Modifier.padding(horizontal = 5.dp),
//        topBar = {
//            //↓これいつの話？
//            //悩みどころだが、BackだとGraphから来たときにGraphに戻る可能性があるので
//            //強制的にMainScreenに行くことにする。しっかり設計しないとヒューマンエラー起きそうだな
//            TopBarView(
//                title = "What is essential is invisible to the eye",
//                onBackNavClicked = {
////                    navController.navigate(Screen.MainScreen.Content.route)
//                    navController.popBackStack()
//                },
//                showBackButton = true
//            )
//        },
//        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
//    ) { innerPadding ->
//        val scrollState = rememberScrollState()
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding)
//                .verticalScroll(scrollState),
//            verticalArrangement = Arrangement.Center
//        ) {
//            if (loadingState) {
//                CircularProgressIndicator()
//                /**
//                 * できれば早期returnではなくて、elseで囲んだほうが良いらしい。
//                 */
//                return@Scaffold
//            }
//            val dateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd")
//            val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
//            val basicModifier = Modifier.width(260.dp)
//
//            /*************************************************/
//            /* 日付時間の項目 */
//            /*************************************************/
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.Absolute.Left
//            ) {
//                TextField(
//                    value = selectedDate?.format(dateFormat)
//                        ?: "日付が入っていません",
//                    onValueChange = {},
//                    enabled = false,
//                    readOnly = true,
//                    label = { Text(text = "Date") },
//                    modifier = Modifier
//                        .width(150.dp)
//                        .clickable {
//                            isDatePickerVisible = true
//                        },
//                    colors = enabledTextFiledColorSet()
//                )
//                /* 日付をクリックしたときにどうなるか */
//                if (isDatePickerVisible) {
//                    DatePickerModal(
//                        onDateSelected = { dateMillis ->
//                            // 選択された日付を処理
//                            dateMillis?.let {
//                                //ただの日付だけ
//                                val _selectedDate =
//                                    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault())
//                                        .toLocalDate()
//                                selectedDate = _selectedDate
//                            }
//                            isDatePickerVisible = false
//                        },
//                        onDismiss = { isDatePickerVisible = false }
//                    )
//                }
//
//                Spacer(modifier = Modifier.padding(8.dp))
//
//                TextField(
//                    value = selectedTime?.format(timeFormat)
//                        ?: "時間が入っていません",
//                    onValueChange = {},
//                    enabled = false,
//                    readOnly = true,
//                    label = { Text(text = "Time") },
//                    modifier = Modifier
//                        .width(100.dp)
//                        .clickable {
//                            isTimePickerVisible = true
//                        },
//                    colors = enabledTextFiledColorSet()
//                )
//
//                //時間をタップしたらダイアログを表示して選択させる
//                //Clickableの中身はComposable関数を入れられないらしい？だからここで分けて書いている
//                if (isTimePickerVisible) {
//                    //nullでないときのみ時刻を表示
//                    DialWithDialog(
//                        onConfirm = {
//                            // 選択した時間を取得して ViewModel に更新
//                            val _newTime = LocalTime.of(it.hour, it.minute)
//                            selectedTime = _newTime
//                            isTimePickerVisible = false
//                        },
//                        onDismiss = {
//                            isTimePickerVisible = false
//                        },
//                        //基本的に値は入っているが、なにかおかしくなった時用にタイムゾーンの時間を入れておく。
//                        initialDateTime = selectedTime?.atDate(selectedDate)
//                            ?: AppTimeZone.getCurrentTimeInZone()
//                    )
//                }
//            }
//
//            /*************************************************/
//            /* 合計金額の表示(分割入力のときのみ!!) */
//            /*************************************************/
//            if (splitInputState) {
//                /**
//                 * trueに入ったときに、index=0の値をトータルに代入する。
//                 * trueからfalseに入ったときは、index=0にこの値を返す
//                 */
//                RowSpace()
//                TextField(
//                    modifier = basicModifier,
//                    value = totalAmount?.toString() ?: "",
//                    onValueChange = {
//                        val convertedVal = it.roundToLongOrNull()/* 自作 */
//                        if (it != "" && convertedVal == null) {
//                            scope.launch {
//                                snackBarHostState.showSnackbar("数値が大きすぎます。これ以上入力できません")
//                            }
//                            //viewModel.updateExpenseInstanceAmount(null)
//                        } else {
//                            viewModel.updateTotalAmount(convertedVal)
//                            showCalculator = false
//                        }
//                    },
//                    label = { Text(text = "合計金額") },
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//                )
//            }
//
//            /* expenseListをぶん回す */
//            expenseList.forEachIndexed { index, expense ->
//                /* 最後の要素は自動で入力できないようにする */
//                val isLastElement =
//                    index == expenseList.size - 1 && splitInputState && index != 0
//                Column(
//                    modifier = if (splitInputState) Modifier.border(
//                        width = 1.dp,
//                        color = MaterialTheme.colorScheme.secondary
//                    ) else Modifier
//                ) {
//
//                    /*************************************************/
//                    /* 費用の項目 */
//                    /*************************************************/
//                    RowSpace()
//                    Row(
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        TextField(
//                            //数値だけ受け付ける感じにしたい
//                            value = expense.amount?.toString() ?: "",
//                            onValueChange = {},
//                            readOnly = true,
//                            enabled = false,
//                            label = { Text(text = "Amount") },
//                            modifier = basicModifier
//                                .clickable {
//                                    if (isLastElement) {
//                                        /* 分割入力の最後の値は入力させない */
//                                        return@clickable
//                                    }
//                                    showCalculator = true
//                                    viewModel.setSelectedIndex(index)
//                                    Log.d(viewName, "Amount was tapped!!!")
//                                },
//                            colors = if (!isLastElement) enabledTextFiledColorSet() else TextFieldDefaults.colors()
//                        )
//
//                        /* 先頭費用しか載せない */
//                        if (index == 0) {
//                            Switch(
//                                checked = splitInputState,
//                                onCheckedChange = {
//                                    if (!splitInputState && expense.amount == null) {
//                                        scope.launch {
//                                            snackBarHostState.currentSnackbarData?.dismiss()
//                                            snackBarHostState.showSnackbar(
//                                                "先頭費用を入力しないとONできません",
//                                                actionLabel = "OK"
//                                            )
//                                        }
//                                    } else {
//                                        viewModel.switchSplitInput()
//                                    }
//                                },
//                                modifier = Modifier.padding(vertical = 0.dp)
//                            )
//                            Text("分割入力")
//                        } else {
//                            /* 削除ボタン */
//                            IconButton(
//                                onClick = {
//                                    viewModel.setSelectedIndex(index)
//                                    viewModel.removeExpenseFromListAtSelectedIndex()
//                                }
//                            ) {
//                                Icon(
//                                    imageVector = Icons.Default.Delete,
//                                    contentDescription = "Delete"
//                                )
//                            }
//                        }
//                    }
//
//                    if (showCalculator) {
//                        ModalBottomSheet(
//                            onDismissRequest = { showCalculator = false },
//                            sheetState = bottomSheetState
//                        ) {
//                            CalculatorUI(
//                                initialValue = viewModel.getExpenseAmountAtSelectedIndex(),
//                                onDecide = {/* 日本円を使っている限りは、整数に変換。おいおい外貨にも対応 */
//                                    if (true/* 日本円。設定から制御できるように、、 */) {
//                                        val convertedVal = it.roundToLongOrNull()/* 自作 */
//                                        if (it != "" && convertedVal == null) {
//                                            scope.launch {
//                                                snackBarHostState.showSnackbar("数値が大きすぎます。これ以上入力できません")
//                                            }
//                                        } else {
//                                            Log.d(viewName, "selected index:${index}")
//                                            showCalculator = false
//                                            viewModel.updateExpenseAmountAtSelectedIndex(
//                                                convertedVal
//                                            )
//                                        }
//                                    } else {
//                                        /* ラウンドしない場合、、 */
//                                    }
//                                }
//                            )
//                        }
//                    }
//
//                    /* カテゴリー割当て */
//                    RowSpace()
//                    Row {
//                        CategoryDropDown(
//                            initialCategory = expense.category,
//                            categories = allCategories,
//                            onCategorySelected = {
//                                viewModel.updateExpenseCategoryAt(index, it)
//                            },
//                            modifier = basicModifier,
//                        )
//
//                        // カテゴリー編集ボタン
//                        IconButton(
//                            onClick = {
//                                //navController.navigate(Screen.GlobalScreen.CategoryAddEdit.route)
//                            }
//                        ) {
//                            Icon(
//                                painter = painterResource(id = R.drawable.baseline_edit_24), // カスタムアイコン
//                                contentDescription = "Edit Category"
//                            )
//                        }
//
//                        IconButton(
//                            onClick = {
//                                viewModel.updateStoredCategories {
//                                    /* カテゴリーを更新する */
//                                }
//                            }
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Refresh,
//                                contentDescription = "Update"
//                            )
//                        }
//                    }
//
//                    /*************************************************/
//                    /* Noteの項目 */
//                    /*************************************************/
//                    RowSpace()
//                    Row(
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        //メモ
//                        TextField(
//                            value = expense.note ?: "",
//                            onValueChange = {
//                                viewModel.updateExpenseNoteAt(index, it)
//                            },
//                            modifier = basicModifier,
//                            label = { Text(text = "Note(空欄可)") },
//                            singleLine = false
//                        )
//                    }
//
//                    /*****************************************************/
//                    /* 商品名 */
//                    /*****************************************************/
//                    RowSpace()
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        TextField(
//                            value = expense.itemName ?: "",
//                            onValueChange = {
//                                viewModel.updateExpenseItemNameAt(index, it)
//                            },
//                            label = { Text(text = "商品名(空欄可)") },
//                            modifier = basicModifier
//                        )
//                        if (fromScreen == FromScreen.SEARCH) {
//                            if (mainType == GeneratedType.MAIL_EXTRACTION && subType != null) {
//                                val templateType = getEmailTemplateTypeByNodeName(subType)
//                                if (templateType != null) {
//                                    val bitResult =
//                                        templateType.categoryAssignFlag and CategoryAssignFlag.PRODUCT_NAME.value
//                                    if (bitResult != 0) {
//                                        CategoryAssignmentArea(
//                                            onClick = {
//                                                assignmentEdited = CategoryAssignment(
//                                                    name = expense.itemName ?: "",
//                                                    categoryId = expense.category?.id,
//                                                    condition = AssignmentCondition.EXACT_MATCH
//                                                )
//                                                namePattern = CategoryAssignNamePattern.PRODUCT
//                                                showCategoryAssignmentDialog = true
//                                            }
//                                        )
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//
//                if (splitInputState && index == viewModel.expenseList.value.size - 1) {
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.Center
//                    ) {
//                        IconButton(
//                            onClick = {
//                                viewModel.addExpenseToList()
//                                viewModel.calcLastExpenseAmount()/* これを実行しておかないと初期値が空になる。 */
//                            }
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Add,
//                                contentDescription = "Add"
//                            )
//                        }
//                    }
//                } else {
//                    RowSpace()
//                }
//            }
//
//            /**
//             * 店名
//             * これは、分割入力の場合でもすべて一緒
//             */
//            RowSpace()
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                TextField(
//                    value = viewModel.getHeadExpense().storeName ?: "",
//                    onValueChange = {
//                        viewModel.updateExpenseStoreName(it)
//                    },
//                    label = { Text(text = "店名(空欄可)") },
//                    modifier = basicModifier
//                )
//
//                /**
//                 * まず、自動カテゴリー追加はNot_CategorizedからこのExpense編集画面に飛んできたときのみ表示
//                 * EmailTemplateTypeにはcategoryAssignFlagがあって、そこにビットで商品名なのか店名なのかを保持している
//                 * AND演算子を使って店名や商品名が入っているかをとり、入っていたらボタンを表示
//                 * */
//                if (fromScreen == FromScreen.SEARCH) {
//                    if (mainType == GeneratedType.MAIL_EXTRACTION && subType != null) {
//                        val templateType = getEmailTemplateTypeByNodeName(subType)
//                        if (templateType != null) {
//                            val bitResult =
//                                templateType.categoryAssignFlag and CategoryAssignFlag.STORE_NAME.value
//                            if (bitResult != 0) {
//                                CategoryAssignmentArea(
//                                    onClick = {
//                                        LogAkitaDebug("Tapped. Is it reactive?")
//                                        assignmentEdited = CategoryAssignment(
//                                            name = viewModel.getHeadExpense().storeName ?: "",
//                                            categoryId = null,/* 分割入力のときはどれを優先すれば良いかわからないのでnullで渡す */
//                                            condition = AssignmentCondition.EXACT_MATCH
//                                        )
//                                        namePattern = CategoryAssignNamePattern.STORE
//                                        showCategoryAssignmentDialog = true
//                                    }
//                                )
//
//                            }
//                        }
//                    }
//                }
//            }
//
//            /*************************************************/
//            /* 生成タイプとサブタイプの表示 */
//            /* 分割入力で共通 */
//            /*************************************************/
//            if (viewModel.getHeadExpense().id != null) {
//                RowSpace()
//                Row(
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    TextField(
//                        value = viewModel.getGeneratedTypeDisplay(),
//                        onValueChange = {},
//                        readOnly = true,
//                        enabled = false,
//                        label = { Text(text = "生成方法(変更不可)") },
//                        modifier = basicModifier,
//                        //colors = enabledTextFiledColorSet()
//                    )
//                }
//            }
//
//            /*************************************************/
//            /* 保存ボタンの実装 */
//            /*************************************************/
//            Button(
//                onClick = {
//                    handleSaveClick()
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(10.dp),
//                colors = ButtonDefaults.textButtonColors(
//                    containerColor = MaterialTheme.colorScheme.primary,
//                    contentColor = MaterialTheme.colorScheme.onPrimary
//                )
//            ) {
//                Text("Save")
//            }
//
//            // 新規作成時：リセット、更新時：削除
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 10.dp),
//                horizontalArrangement = Arrangement.Start
//            ) {
//                if (viewModel.getHeadExpense().id == null) {
//                    Button(
//                        onClick = {
//                            viewModel.resetExpenseList()
//                        },
//                        modifier = Modifier
//                            .width(140.dp)
//                            .padding(4.dp),
//                        colors = ButtonDefaults.textButtonColors(
//                            containerColor = MaterialTheme.colorScheme.secondary,
//                            contentColor = MaterialTheme.colorScheme.onSecondary
//                        )
//                    ) {
//                        Text("Reset")
//                    }
//                } else {
//                    Button(
//                        onClick = {
//                            showDeleteResetConfirmDialog = true
//                        },
//                        modifier = Modifier
//                            .width(140.dp)
//                            .padding(4.dp),
//                        colors = ButtonDefaults.textButtonColors(
//                            containerColor = MaterialTheme.colorScheme.error,
//                            contentColor = MaterialTheme.colorScheme.onError
//                        )
//                    ) {
//                        Text("Delete")
//                    }
//                }
//            }
//        }
//        /**
//         * 新しいカテゴリー割当のダイアログ
//         */
//        if (showCategoryAssignmentDialog) {
//            /* メール抽出/楽天Payに保存するって書いておいた法が良いな */
//            CategoryAssignmentDialog(
//                titleContent = {
//                    Text(viewModel.getGeneratedTypeDisplay())
//                },
//                onSave = { assignment, namePattern ->
//                    viewModel.addCategoryAssignment(
//                        onStart = {},
//                        assignment,
//                        namePattern,
//                        callback = {
//                            if (it.status == FuncStatus.SUCCESS) {
//                                showCategoryAssignmentDialog = false
//                                scope.launch {
//                                    snackBarHostState.currentSnackbarData?.dismiss()
//                                    snackBarHostState.showSnackbar(
//                                        "カテゴリー割当に追加されました。これ以降は自動でカテゴリーが割り当てられます。",
//                                        actionLabel = "OK"
//                                    )
//                                }
//                            } else {
//                                scope.launch {
//                                    snackBarHostState.currentSnackbarData?.dismiss()
//                                    snackBarHostState.showSnackbar(
//                                        "カテゴリー割当追加に失敗しました。${it.errorMessage}",
//                                        actionLabel = "OK"
//                                    )
//                                }
//                            }
//                        })
//                },
//                onDismiss = {
//                    showCategoryAssignmentDialog = false
//                },
//                initialAssignment = assignmentEdited,
//                categories = allCategories,
//                initialNamePattern = namePattern,
//                isNamePatternSelectable = false
//            )
//        }
//
//        /**
//         * 削除関数のダイアログ
//         */
//        if (showDeleteResetConfirmDialog) {
//            ConfirmAlertDialog(
//                confirmContent = {
//                    Text(
//                        text = "削除しますか？",
//                        modifier = Modifier.padding(horizontal = 20.dp),
//                        fontSize = 20.sp
//                    )
//                },
//                onClick = {
//                    viewModel.removeExpenseToDb(
//                        onStart = {
//                            viewModel.setLoadingState(true)
//                            showDeleteResetConfirmDialog = false
//                        },
//                        callback = {
//                            if (it.status == FuncStatus.SUCCESS) {
//                                viewModel.resetExpenseList()
//                                navController.popBackStack()
//                            } else {
//                                Toast.makeText(
//                                    context,
//                                    "削除に失敗しました:${it.errorMessage}",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                        }
//                    )
//                },
//                onDismissRequest = {
//                    showDeleteResetConfirmDialog = false
//                },
//
//                )
//        }
//    }
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
    var input by remember { mutableStateOf(if (initialValue == 0L) "" else initialValue.toString()) }

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