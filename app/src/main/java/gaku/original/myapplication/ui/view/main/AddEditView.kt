package gaku.original.myapplication.ui.view.main

import android.annotation.SuppressLint
import android.util.Log
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Interface.CategoryAssignFlag
import gaku.original.myapplication.data.Interface.CategoryAssignNamePattern
import gaku.original.myapplication.data.dataClass.AssignmentCondition
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.data.dataClass.GeneratedType
import gaku.original.myapplication.data.dataClass.getEmailTemplateTypeByNodeName
import gaku.original.myapplication.ui.common.CategoryAssignmentDialog
import gaku.original.myapplication.ui.common.ConfirmAlertDialog
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.ui.common.enabledTextFiledColorSet
import gaku.original.myapplication.utility.AppTimeZone
import gaku.original.myapplication.utility.LogAkitaDebug
import gaku.original.myapplication.utility.evalExpression
import gaku.original.myapplication.utility.roundToLongOrNull
import gaku.original.myapplication.viewModel.main.ExpenseAddEditViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/*
・まずはすべて手入力で実装する
FloatingActionボタンから来た場合は、ボタンを叩いた時間を入力
カレンダーの日付を叩いてきたときはその日付と時間(今の時間)をデフォルトでいれる
 */
enum class FromScreen {
    NOT_CATEGORIZED,
    MAIN_CONTENT,
    UNKNOWN
}

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseAddEditView(
    viewModel: ExpenseAddEditViewModel = hiltViewModel(),
    navController: NavController,
    from: String/* 遷移元のスクリーン */
) {
    val fromScreen = when (from) {
        Screen.NotCategorizedScreen.route -> FromScreen.NOT_CATEGORIZED
        Screen.MainScreen.Content.route -> FromScreen.MAIN_CONTENT
        else -> FromScreen.UNKNOWN
    }

    val context = LocalContext.current

    val viewName = "ExpenseAddEditView"

    var selectedDate by remember { mutableStateOf<LocalDate?>(viewModel.getTimeZoneDate()) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(viewModel.getTimeZoneTime()) }

    //日付、時間の選択肢用
    var isDatePickerVisible by remember { mutableStateOf(false) }
    var isTimePickerVisible by remember { mutableStateOf(false) }

    //計算機用
    var showCalculator by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    /* StateFlowあたりよくわかっていない、、ちゃんと勉強しないと */
    val allCategories by viewModel.allCategories.collectAsState(initial = emptyList())

    var categoryOptionsExpanded by remember { mutableStateOf(false) }

    /* 割当をするかしないか問題 */
    var showCategoryAssignmentDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteResetConfirmDialog by remember { mutableStateOf(false) }
    var namePattern by remember { mutableStateOf(CategoryAssignNamePattern.STORE) }
    var assignmentEdited by remember { mutableStateOf(CategoryAssignment()) }

    val scope = rememberCoroutineScope()
    val snackBarHostState = remember {
        SnackbarHostState()
    }


    fun handleSaveClick() {
        if (selectedDate == null || selectedTime == null) {
            scope.launch {
                snackBarHostState.showSnackbar("日付と時間が選択されていません", actionLabel = "OK")
            }
            return
        }

        if (viewModel.currentTmpExpense.amount == null) {
            scope.launch {
                snackBarHostState.showSnackbar("金額が入力されていません", actionLabel = "OK")
            }
            return
        }

        val dateTime = LocalDateTime.of(selectedDate, selectedTime)
        val isoStr = AppTimeZone.localDateTimeToIsoString(dateTime)
        if (isoStr == null) {
            scope.launch {
                snackBarHostState.showSnackbar(
                    "日付と時間の変換に失敗しました\n管理者に連絡してください",
                    actionLabel = "OK"
                )
            }
        }
//        LogAkitaDebug("converted String ${isoStr}")
        /* isoStrがnullであることはない。上でチェックしている。 */
        viewModel.updateTmpExpenseDatetime(isoStr!!)

        /* カテゴリーをリモートから取得できなかったとき保存も更新もできなくなる。したがって、カテゴリーのチェックはやめる */
        /* あるいは、allCategoriesがemptyList()のときだけこのチェックをバイパスするとかでもいいな */
//        if (viewModel.currentTmpExpense.category == null) {
//            scope.launch {
//                snackBarHostState.showSnackbar(
//                    "カテゴリーを選択してください"
//                )
//            }
//        }

        if (viewModel.currentTmpExpense.id == null) {
            viewModel.addTmpExpenseToDb(onStart = {}, callback = {
                /* 成功のときのみ */
                if (it.status == SuspendFuncStatus.SUCCESS) {
                    scope.launch {
                        snackBarHostState.showSnackbar("追加しました")
                    }
                    navController.popBackStack()
                }
            })
        } else {
            viewModel.updateTmpExpenseToDb(onStart = {}, callback = {
                if (it.status == SuspendFuncStatus.SUCCESS) {
                    scope.launch {
                        snackBarHostState.showSnackbar("更新する")
                    }
                    navController.popBackStack()
                }
            })
        }
    }

    val typeList = viewModel.getSeparatedGeneratedType()
    val mainType = typeList.getOrNull(0)
    val subType = typeList.getOrNull(1)

    /* デバッグ用 */
    LaunchedEffect(allCategories) {
        viewModel.getTimeZoneDate()
        Log.d("UI", "Categories updated in UI: $allCategories")
    }

    Scaffold(
        modifier = Modifier.padding(horizontal = 5.dp),
        topBar = {
            //↓これいつの話？
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
        //bottomBar = { BottomBarView(navController) }
    ) { innerPadding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center
        ) {
            val dateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd")
            val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
            val basicModifier = Modifier.width(260.dp)

            /*************************************************/
            /* 日付の項目 */
            /*************************************************/
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Absolute.Left
            ) {
                TextField(
                    value = selectedDate?.format(dateFormat)
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
                                //ただの日付だけ
                                val _selectedDate =
                                    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                                selectedDate = _selectedDate
                            }
                            isDatePickerVisible = false
                        },
                        onDismiss = { isDatePickerVisible = false }
                    )
                }

                Spacer(modifier = Modifier.padding(8.dp))

                TextField(
                    value = selectedTime?.format(timeFormat)
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
                    DialWithDialog(
                        onConfirm = {
                            // 選択した時間を取得して ViewModel に更新
                            val _newTime = LocalTime.of(it.hour, it.minute)
                            selectedTime = _newTime
                            isTimePickerVisible = false
                        },
                        onDismiss = {
                            isTimePickerVisible = false
                        },
                        //基本的に値は入っているが、なにかおかしくなった時用にタイムゾーンの時間を入れておく。
                        initialDateTime = selectedTime?.atDate(selectedDate)
                            ?: AppTimeZone.getCurrentTimeInZone()
                    )
                }
            }


            RowSpace()

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
                    modifier = basicModifier
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
                        initialValue = viewModel.currentTmpExpense.amount ?: 0L,
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

            RowSpace()
            /*************************************************/
            /* カテゴリーの項目 */
            /*************************************************/
            Row()
            {
                ExposedDropdownMenuBox(
                    expanded = categoryOptionsExpanded,
                    onExpandedChange = {
                        if (!categoryOptionsExpanded) {
                            //@TODO ここうまく機能していない。
                            if (allCategories.isEmpty()) {/* allCategories.isEmpty() */
                                viewModel.updateStoredCategories { statusInfo ->
                                    if (statusInfo.status == SuspendFuncStatus.SUCCESS) {
                                        LogAkitaDebug("Properly fetched categories　${allCategories}")
                                        if (allCategories.isEmpty()) {
                                            //何もなかったらToastを出す
                                            /* だめだ。思ったとおりに動かん。 */
//                                            scope.launch {//なんかここエラー出るぞ？
//                                                snackBarHostState.showSnackbar(
//                                                    "カテゴリーが何も登録されていません。\n編集ボタンから追加してください",
//                                                    actionLabel = "OK",
//                                                    duration = SnackbarDuration.Short
//                                                )
//                                            }
                                        } else {
                                            /* ちゃんとカテゴリーがあるので表示する */
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
                        modifier = basicModifier
                            //@Todo menuAnchorをいれかえる　
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
                        navController.navigate(Screen.GlobalScreen.CategoryAddEdit.route)
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

            RowSpace()

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
                    modifier = basicModifier,
                    label = { Text(text = "Note(空欄可)") },
                    singleLine = false
                )
            }

            /*************************************************/
            /* 生成タイプとサブタイプの表示 */
            /*************************************************/
            if (viewModel.currentTmpExpense.id != null) {
                RowSpace()
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = viewModel.getGeneratedTypeDisplay(),
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text(text = "生成方法(変更不可)") },
                        modifier = basicModifier,
                        //colors = enabledTextFiledColorSet()
                    )
                }
            }

            /*************************************************/
            /* 店名や商品名の表示 */
            /* 普通のやつにもつけておくわ */
            /* 使ってみていらなそうだったら表示のみor Not Categorizedのときのみに変更 */
            /*************************************************/
            /* @TODO 検索候補をつけたい */
            RowSpace()
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = viewModel.currentTmpExpense.itemName ?: "",
                    onValueChange = {
                        viewModel.updateTmpExpenseItemName(it)
                    },
                    label = { Text(text = "商品名(空欄可)") },
                    modifier = basicModifier
                )
                if (fromScreen == FromScreen.NOT_CATEGORIZED) {
                    if (mainType == GeneratedType.MAIL_EXTRACTION && subType != null) {
                        val templateType = getEmailTemplateTypeByNodeName(subType)
                        if (templateType != null) {
                            val bitResult =
                                templateType.categoryAssignFlag and CategoryAssignFlag.PRODUCT_NAME.value
                            if (bitResult != 0) {
                                CategoryAssignmentArea(
                                    onClick = {
                                        assignmentEdited = CategoryAssignment(
                                            name = viewModel.currentTmpExpense.itemName ?: "",
                                            categoryId = viewModel.currentTmpExpense.category?.id,
                                            condition = AssignmentCondition.EXACT_MATCH
                                        )
                                        namePattern = CategoryAssignNamePattern.PRODUCT
                                        showCategoryAssignmentDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            RowSpace()
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = viewModel.currentTmpExpense.storeName ?: "",
                    onValueChange = {
                        viewModel.updateTmpExpenseStoreName(it)
                    },
                    label = { Text(text = "店名(空欄可)") },
                    modifier = basicModifier
                )

                /**
                 * まず、自動カテゴリー追加はNot_CategorizedからこのExpense編集画面に飛んできたときのみ表示
                 * EmailTemplateTypeにはcategoryAssignFlagがあって、そこにビットで商品名なのか店名なのかを保持している
                 * AND演算子を使って店名や商品名が入っているかをとり、入っていたらボタンを表示
                 * */
                if (fromScreen == FromScreen.NOT_CATEGORIZED) {
                    if (mainType == GeneratedType.MAIL_EXTRACTION && subType != null) {
                        val templateType = getEmailTemplateTypeByNodeName(subType)
                        if (templateType != null) {
                            val bitResult =
                                templateType.categoryAssignFlag and CategoryAssignFlag.STORE_NAME.value
                            if (bitResult != 0) {
                                CategoryAssignmentArea(
                                    onClick = {
                                        LogAkitaDebug("Tapped. Is it reactive?")
                                        assignmentEdited = CategoryAssignment(
                                            name = viewModel.currentTmpExpense.storeName ?: "",
                                            categoryId = viewModel.currentTmpExpense.category?.id,
                                            condition = AssignmentCondition.EXACT_MATCH
                                        )
                                        namePattern = CategoryAssignNamePattern.STORE
                                        showCategoryAssignmentDialog = true
                                    }
                                )

                            }
                        }
                    }
                }
            }

            /*************************************************/
            /* 保存ボタンの実装 */
            /*************************************************/
            Button(
                onClick = {
                    handleSaveClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Save")
            }

            // 新規作成時：リセット、更新時：削除
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                if (viewModel.currentTmpExpense.id == null) {
                    Button(
                        onClick = {
                            viewModel.resetTmpExpense()
                        },
                        modifier = Modifier
                            .width(140.dp)
                            .padding(4.dp),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Text("Reset")
                    }
                } else {
                    Button(
                        onClick = {
                            showDeleteResetConfirmDialog = true
                        },
                        modifier = Modifier
                            .width(140.dp)
                            .padding(4.dp),
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
        /**
         * 新しいカテゴリー割当のダイアログ
         */
        if (showCategoryAssignmentDialog) {
            /* メール抽出/楽天Payに保存するって書いておいた法が良いな */
            CategoryAssignmentDialog(
                titleContent = {
                    Text(viewModel.getGeneratedTypeDisplay())
                },
                onSave = { assignment, namePattern ->
                    viewModel.addCategoryAssignment(
                        onStart = {},
                        assignment,
                        namePattern,
                        callback = {
                            if (it.status == SuspendFuncStatus.SUCCESS) {
                                showCategoryAssignmentDialog = false
                                scope.launch {
                                    snackBarHostState.currentSnackbarData?.dismiss()
                                    snackBarHostState.showSnackbar(
                                        "カテゴリー割当に追加されました。これ以降は自動でカテゴリーが割り当てられます。",
                                        actionLabel = "OK"
                                    )
                                }
                            } else {
                                scope.launch {
                                    snackBarHostState.currentSnackbarData?.dismiss()
                                    snackBarHostState.showSnackbar(
                                        "カテゴリー割当追加に失敗しました。${it.errorMessage}",
                                        actionLabel = "OK"
                                    )
                                }
                            }
                        })
                },
                onDismiss = {
                    showCategoryAssignmentDialog = false
                },
                initialAssignment = assignmentEdited,
                categories = allCategories,
                initialNamePattern = namePattern,
                isNamePatternSelectable = false
            )
        }

        /**
         * 削除関数のダイアログ
         */
        if (showDeleteResetConfirmDialog) {
            ConfirmAlertDialog(
                confirmContent = {
                    Text(
                        text = "削除しますか？",
                        modifier = Modifier.padding(horizontal = 20.dp),
                        fontSize = 20.sp
                    )
                },
                onClick = {
                    viewModel.removeTmpExpenseToDb(
                        onStart = {},
                        callback = {
                            if (it.status == SuspendFuncStatus.SUCCESS) {
                                viewModel.resetTmpExpense()
                                navController.popBackStack()
                            } else {
                                Toast.makeText(
                                    context,
                                    "削除に失敗しました:${it.errorMessage}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                },
                onDismissRequest = {
                    showDeleteResetConfirmDialog = false
                },

                )
        }
    }
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