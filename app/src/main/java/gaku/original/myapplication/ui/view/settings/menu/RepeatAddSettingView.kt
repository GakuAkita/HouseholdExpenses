package gaku.original.myapplication.ui.view.settings.menu

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import gaku.original.myapplication.data.Constants.DayOfWeek
import gaku.original.myapplication.data.Constants.RepeatFrequency
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Constants.getRepeatFrequencyValues
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.Frequency
import gaku.original.myapplication.data.dataClass.RepeatAdd
import gaku.original.myapplication.data.dataClass.defaultFrequency
import gaku.original.myapplication.data.dataClass.defaultRepeatAdd
import gaku.original.myapplication.ui.common.BottomBarView
import gaku.original.myapplication.ui.common.CategoryDropDown
import gaku.original.myapplication.ui.common.ConfirmAlertDialog
import gaku.original.myapplication.ui.common.SwipeToRevealItem
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.ui.common.enabledTextFiledColorSet
import gaku.original.myapplication.ui.view.main.DialWithDialog
import gaku.original.myapplication.utility.AppTimeZone
import gaku.original.myapplication.utility.LogAkitaDebug
import gaku.original.myapplication.utility.getLastDayOfMonth
import gaku.original.myapplication.viewModel.settings.RepeatAddViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepeatAddSettingView(
    viewModel: RepeatAddViewModel = hiltViewModel(),
    navController: NavController
) {
    val funcName = "RepeatAddSettingView"

    var showAddEditDialog by remember { mutableStateOf(false) }
    var showAddExpenseConfirmDialog by remember { mutableStateOf(false) }

    val progress by viewModel.progress.collectAsState()
    var expenseAddLoading by remember { mutableStateOf(false) }

    var editedRepeatAdd by remember { mutableStateOf(defaultRepeatAdd) }

    val allCategories = viewModel.allCategories

    val repeatAddSettings by viewModel.repeatAddSettings.collectAsState(initial = emptyList())

    val listState = rememberLazyListState()

    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchAllRepeatAddSettings()
    }

    Scaffold(
        topBar = {
            TopBarView("繰り返し追加", onBackNavClicked = {
                navController.popBackStack()
            }, showBackButton = true)
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        bottomBar = { BottomBarView(navController) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 30.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {
                Text("毎月1日に自動で追加されます")
            }
            if (repeatAddSettings.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("左にスワイプすると削除ボタンが現れます")
                }
            }
            /**
             * ここで検索とかできるようにしたいなあ～
             */
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                userScrollEnabled = true
            ) {
                items(repeatAddSettings) { repeatAdd ->
                    RepeatAddItem(
                        repeatAdd,
                        onEdit = {
                            /* 編集をしたい */
                            editedRepeatAdd = repeatAdd
                            showAddEditDialog = true
                        },
                        onDelete = {
                            /* 削除をする */
                            viewModel.removeRepeatAdd(repeatAdd, callback = { status ->
                                when (status.status) {
                                    SuspendFuncStatus.SUCCESS -> {
                                        scope.launch {
                                            snackBarHostState.showSnackbar(
                                                "削除しました",
                                                actionLabel = "OK"
                                            )
                                        }
                                        /* 再度読み込む、、 */
                                        viewModel.fetchAllRepeatAddSettings()
                                    }

                                    SuspendFuncStatus.TIMEOUT -> {
                                        scope.launch {
                                            snackBarHostState.showSnackbar(
                                                "削除できませんでした。タイムアウトしました",
                                                actionLabel = "OK"
                                            )
                                        }
                                    }

                                    SuspendFuncStatus.FAILED -> {
                                        scope.launch {
                                            snackBarHostState.showSnackbar(
                                                "削除に失敗しました",
                                                actionLabel = "OK"
                                            )
                                        }
                                    }
                                }
                            })
                        })
                }
            }

            Button(
                onClick = {
                    editedRepeatAdd = defaultRepeatAdd
                    showAddEditDialog = true
                }
            ) {
                Text("追加する")
            }

            if (showAddEditDialog) {
                RepeatAddEditDialog(
                    repeatAdd = editedRepeatAdd,
                    allCategories = allCategories,
                    onSave = { newRepeatAdd ->
                        if (newRepeatAdd.id == null)//新規追加
                        {
                            viewModel.addRepeatAddSetting(newRepeatAdd, callback = { result ->
                                if (result is FuncResultWithData.Success) {
                                    viewModel.fetchAllRepeatAddSettings()
                                    showAddEditDialog = false

                                    /* 新規追加のときは、こいつをtrueにして、このあと月末まで追加するか選ばせる */
                                    editedRepeatAdd = result.data
                                    showAddExpenseConfirmDialog = true
                                } else {
                                    /* do nothing */
                                    Toast.makeText(
                                        context,
                                        result.toSuspendFuncStatusInfo().errorMessage,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                        } else {/* 編集 */
                            viewModel.updateRepeatAdd(newRepeatAdd, callback = { status ->
                                if (status.status == SuspendFuncStatus.SUCCESS) {
                                    viewModel.fetchAllRepeatAddSettings()
                                    showAddEditDialog = false
                                } else {
                                    /* do nothing */
                                    Toast.makeText(context, status.errorMessage, Toast.LENGTH_SHORT)
                                        .show()
                                }
                            })
                        }
                    },
                    onDismiss = {
                        showAddEditDialog = false
                        editedRepeatAdd = defaultRepeatAdd
                    },
                    context = context
                )
            }

            /* 月末まで追加するときの確認をする */
            if (showAddExpenseConfirmDialog) {
                ConfirmAlertDialog(
                    onClick = {
                        viewModel.initProgress()
                        showAddExpenseConfirmDialog = false
                        expenseAddLoading = true
                        viewModel.addExpensesForRestOfDays(
                            editedRepeatAdd
                        ) { status ->
                            /**
                             * インジケーターの表示を消して、
                             * snackbarで成功なのか失敗なのかを伝える
                             */
                            expenseAddLoading = false
                            scope.launch {
                                snackBarHostState.currentSnackbarData?.dismiss()
                                snackBarHostState.showSnackbar(
                                    status.errorMessage,
                                    actionLabel = "OK"
                                )
                            }
                        }
                    },
                    onDismissRequest = {
                        /**
                         * 追加のときも編集のときもeditedRepeatAddはボタン押下時に初期化されるから
                         * ここで初期化はしなくて良い
                         */
                        showAddExpenseConfirmDialog = false
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                vertical = 20.dp,
                                horizontal = 10.dp
                            )
                    ) {
                        Text("今月翌日から月末まで費用を追加しますか？\n")
                    }
                }
            }

            if (expenseAddLoading) {
                BasicAlertDialog(
                    onDismissRequest = {}
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("費用追加中...")
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RepeatAddItem(repeatAdd: RepeatAdd, onEdit: () -> Unit = {}, onDelete: () -> Unit = {}) {
    val fontSize = 20.sp

    /* スクリーンの横幅を取得する */
    val configuration = LocalConfiguration.current
    val screenWidthPx = configuration.screenWidthDp.dp
    val density = LocalDensity.current
    val horizontalMaxOffset = with(density) { screenWidthPx.toPx() * 0.2f }

    SwipeToRevealItem(
        horizontalMaxOffset = -horizontalMaxOffset,
        backgroundColor = MaterialTheme.colorScheme.error,
        hiddenContent = {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(with(density) { (horizontalMaxOffset).toDp() })/* 引き出し領域の中央にDeleteアイコンを配置 */,
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        },
        onClick = {
            onDelete()
        },
    ) {
        Row(
            modifier = Modifier
                .border(width = 1.dp, color = MaterialTheme.colorScheme.onSecondary)
                .clickable {
                    onEdit()
                }
                .padding(vertical = 5.dp, horizontal = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${repeatAdd.frequencyInfo.frequency?.replace("_", " ")}",
                modifier = Modifier.weight(1f),
                fontSize = fontSize,
                textAlign = TextAlign.Left//左寄せ
            )
            Text(
                text = "${repeatAdd.expense.amount}",
                modifier = Modifier.weight(1f),
                fontSize = fontSize,
                textAlign = TextAlign.Left,//左寄せ
            )
            Text(
                text = "${repeatAdd.expense.category?.name}",
                modifier = Modifier.weight(1f),
                fontSize = 20.sp,
                textAlign = TextAlign.Left
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepeatAddEditDialog(
    repeatAdd: RepeatAdd,//ここで引数を渡すのは、編集に対応できるようにするため
    allCategories: StateFlow<List<Category>>,
    onSave: (repeatAdd: RepeatAdd) -> Unit,
    onDismiss: () -> Unit,
    context: Context
) {
    var newRepeatAdd by remember { mutableStateOf(repeatAdd.copy()) }

    val categories = allCategories.collectAsState()
    var categoryOptionsExpanded by remember { mutableStateOf(false) }
    var amountWarning by remember { mutableStateOf(false) }

    var frequencyOptionsExpanded by remember { mutableStateOf(false) }

    //rememberつけなくてもよいのだが、再コンポーズのたびに関数が呼ばれるのはもったいないので。
    val repeatFrequencyArray = remember { getRepeatFrequencyValues() }

    LaunchedEffect(amountWarning) {
        //amountWarningは表示したらすぐ消す
        if (amountWarning) {
            Toast.makeText(context, "これ以上入力できません。数値が大きすぎます", Toast.LENGTH_SHORT)
                .show()
            delay(2000)

            amountWarning = false
        }
    }

    AlertDialog(
        title = {
            if (repeatAdd.id == null) {
                Text("Add RepeatAdd Setting")
            } else {
                Text("Edit RepeatAdd Setting")
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row {
                    Text("Expense")
                }
                Column(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        .padding(5.dp)
                ) {
                    //Expenseの領域
                    TextField(
                        value = newRepeatAdd.expense.amount?.toString() ?: "",
                        onValueChange = {
                            if (it != "" && it.toLongOrNull() == null) {
                                /* Do nothing */
                                /* キーボードが数値になっているのでエラーが出ることはないが... */
                                /* デバッグ中にパソコンから文字をいれることはできなくない笑 */
                                amountWarning = true
                            } else {
                                amountWarning = false
                                newRepeatAdd = newRepeatAdd.copy(
                                    expense = newRepeatAdd.expense.copy(
                                        amount = it.toLongOrNull()
                                    )
                                )
                            }
                        },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = amountWarning,
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    CategoryDropDown(
                        initialCategoryId = newRepeatAdd.expense.category?.id,
                        categories = categories.value,
                        onCategorySelected = {
                            newRepeatAdd = newRepeatAdd.copy(
                                expense = newRepeatAdd.expense.copy(
                                    category = it
                                )
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    TextField(
                        value = newRepeatAdd.expense.note ?: "",
                        onValueChange = {
                            newRepeatAdd = newRepeatAdd.copy(
                                expense = newRepeatAdd.expense.copy(
                                    note = it
                                )
                            )
                        },
                        label = { Text(text = "Note") },
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.padding(6.dp))

                Row {
                    Text("Frequency")
                }
                Column(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        .padding(5.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = frequencyOptionsExpanded,
                        onExpandedChange = {
                            frequencyOptionsExpanded = !frequencyOptionsExpanded
                        }
                    ) {
                        //カテゴリー(選択肢から選んでもらいたい。RoomDB?)
                        //@Todo タップしたら画面右からスライドして選択肢が入った列が出てくる感じ
                        //とりあえずこれで一応は凌ぐが、本当はもっと使いやすくしたい。
                        TextField(
                            value = newRepeatAdd.frequencyInfo.frequency?.replace("_", " ") ?: "",
                            onValueChange = {/* ドロップダウンから選択すれば値が更新される */ },
                            enabled = false,
                            readOnly = true,
                            modifier = Modifier
                                .width(260.dp)
                                .menuAnchor(),//menuAnchorをつけないとだめっぽいな。
                            label = { Text(text = "Frequency") },
                            singleLine = true,
                            colors = enabledTextFiledColorSet(),
                        )

                        ExposedDropdownMenu(
                            expanded = frequencyOptionsExpanded,
                            onDismissRequest = { frequencyOptionsExpanded = false }
                        ) {
                            repeatFrequencyArray.forEachIndexed { _, freq ->
                                DropdownMenuItem(
                                    text = { Text(text = freq.replace("_", " ")) },
                                    onClick = {
                                        newRepeatAdd = newRepeatAdd.copy(
                                            /* ドロップダウンでfrequencyを変えたときは、FrequencyTextField内のnewFrequencyInfoをリセット */
                                            /* ここの値が初期値としてセットされる */
                                            frequencyInfo = newRepeatAdd.frequencyInfo.copy(
                                                frequency = freq,
                                                month = null,
                                                day = null,
                                                dayOfWeek = null,
                                                hour = 0,/* デフォルトで00:00に設定しておく */
                                                minute = 0
                                            )
                                        )
                                        frequencyOptionsExpanded = false
                                        LogAkitaDebug(
                                            "DropdownMenu was selected.${newRepeatAdd}"
                                        )
                                    }
                                )

                            }
                        }

                    }

                    if (newRepeatAdd.frequencyInfo.frequency != null) {
                        Log.d("AkitaDebug", "FrequencyTextField was called!!")

                        /**
                         * しょうがないが、関数にする以上、FrequencyTextFieldの中でnewFrequencyInfoを書き換え時に、
                         * callbackでnewRepeatAddを書き換えることになる。で、callbackでnewRepeatAddを書き換えると
                         * またFrequencyTextFieldが実行されることになる。
                         * 全部ベタ書きでFrequencyTextFieldの中身を書けばよいのは良いいのだが、見づらくなってしまうので分けた。
                         * @FIXME 関数の実行を1回だけにするような書き方ができる気がするので余裕があったら直そう。
                         */
                        //ここで、選択された内容に応じて表示内容を変える
                        FrequencyTextField(newRepeatAdd.frequencyInfo,
                            callback = {
                                newRepeatAdd = newRepeatAdd.copy(frequencyInfo = it)
                                LogAkitaDebug(
                                    "callback of FrequencyTextField:${newRepeatAdd}"
                                )
                            }
                        )
                        LogAkitaDebug("FrequencyTextField ended??${newRepeatAdd}")
                        //newRepeatAddをTextFieldで変更した時、これだと2回このFrequencyTextFieldが呼ばれてるわ。
                    }
                }

            }
        },
        onDismissRequest = {
            onDismiss()
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    modifier = Modifier
                        .padding(start = 10.dp),
                    onClick = { onDismiss() },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text("Cancel")
                }

                Button(
                    modifier = Modifier
                        .padding(end = 10.dp),
                    onClick = {
                        onSave(newRepeatAdd)
                    }
                ) {
                    Text("Save")
                }
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = true
        )
    )
}


/**
 *  every_year: 何月何日何時(時刻はデフォルトで0)
 *  every_month: 何日何時
 *  weekends : 何時
 *  weekdays : 何時
 *  everyday : 何時
 */

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun FrequencyTextField(
    frequencyInfo: Frequency,
    callback: (Frequency) -> Unit = {}
) {
    LogAkitaDebug("This is inside of FrequencyTextField:${frequencyInfo}")

    val constSpacer = @Composable {
        Spacer(modifier = Modifier.padding(10.dp))
    }

    var day by remember { mutableStateOf<Int?>(null) }
    val time by remember { mutableStateOf(LocalTime.MIDNIGHT) }

    var newFrequencyInfo by remember { mutableStateOf(defaultFrequency) }
    LaunchedEffect(frequencyInfo.frequency) {
        /* frequencyInfoが前と変わったときは、パラメータをdayやdayOfWeekなど含めてリセットする */
        /*  */
        newFrequencyInfo = frequencyInfo
    }
    newFrequencyInfo = newFrequencyInfo.copy(
        frequency = frequencyInfo.frequency
    )
    /* 上書き必要 */
    LogAkitaDebug("This is inside of FrequencyTextField before when:${newFrequencyInfo}")

    var isTimePickerVisible by remember { mutableStateOf(false) }

    when (newFrequencyInfo.frequency) {
        /*******************************************************/
        RepeatFrequency.EVERY_YEAR -> {
            newFrequencyInfo = newFrequencyInfo.copy(
                frequency = RepeatFrequency.EVERY_YEAR,
                dayOfWeek = null,//曜日は必要ない,
            )
            constSpacer()
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column {
                    Text("Month")
                    TextField(
                        modifier = Modifier.width(50.dp),
                        value = newFrequencyInfo.month?.toString() ?: "",
                        onValueChange = {
                            LogAkitaDebug("month onValueChange ${it}")
                            val monthInt = it.toIntOrNull()
                            /* これ日付がちゃんと存在するかもチェックしたほうが良いな */
                            if (it == "" || monthInt == null) {
                                newFrequencyInfo = newFrequencyInfo.copy(
                                    month = null
                                )
                            } else if (monthInt > 12 || monthInt < 1) {
                                /* Do nothing */
                            } else {
                                newFrequencyInfo = newFrequencyInfo.copy(
                                    month = monthInt
                                )
                                LogAkitaDebug("overwritten as ${newFrequencyInfo}")
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                }

                Spacer(modifier = Modifier.padding(10.dp))

                Column()
                {
                    Text("Day")
                    TextField(
                        modifier = Modifier.width(50.dp),
                        value = newFrequencyInfo.day?.toString() ?: "",
                        onValueChange = {
                            LogAkitaDebug("Here month onValueChange${it}")
                            val dayInt = it.toIntOrNull()
                            if (newFrequencyInfo.month == null) {
                                /* monthを入力してください。snack barをだしたい */
                            } else if (it == "" || dayInt == null) {
                                day = null
                                newFrequencyInfo = newFrequencyInfo.copy(
                                    day = null
                                )
                            } else if (dayInt < 1 || dayInt > getLastDayOfMonth(
                                    year = 2025,/* うるう年でなければ何の年でも良い */
                                    month = newFrequencyInfo.month
                                        ?: 1/* 上でmonthがnullだったら入力できないようになっているからここでmonthがnullになることはない */
                                ).dayOfMonth
                            ) {
                                /* 日付が適切でない */
                                /* 例えば、31日がない月は30日(月の最終日に追加される) */
                            } else {
                                newFrequencyInfo = newFrequencyInfo.copy(
                                    day = dayInt
                                )
                                LogAkitaDebug(
                                    "month onValueChange newFrequencyInfo:${newFrequencyInfo}"
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                }

                Spacer(modifier = Modifier.padding(10.dp))

                Column {
                    //val tmpFrequencyInfo = newFrequencyInfo
                    Text("Time")
                    TextField(
                        value = newFrequencyInfo.let { info ->
                            if (info.hour != null && info.minute != null) {
                                "%02d:%02d".format(info.hour, info.minute)
                            } else {
                                ""
                            }
                        },
                        onValueChange = {},
                        enabled = false,
                        readOnly = true,
                        modifier = Modifier
                            .width(80.dp)
                            .clickable {
                                isTimePickerVisible = true
                            },
                        colors = enabledTextFiledColorSet()
                    )
                }


            }
        }

        /*******************************************************/
        RepeatFrequency.EVERY_MONTH -> {
            newFrequencyInfo = newFrequencyInfo.copy(
                frequency = RepeatFrequency.EVERY_MONTH,
                month = null,
                dayOfWeek = null //曜日は必要ない
            )
            constSpacer()
            //何日だけ決定。31がない月は最終日に指定
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column()
                {
                    Text("Day")
                    TextField(
                        modifier = Modifier.width(50.dp),
                        value = newFrequencyInfo.day?.toString() ?: "",
                        onValueChange = {
                            val dayInt = it.toIntOrNull()
                            if (it == "" || dayInt == null) {
                                newFrequencyInfo = newFrequencyInfo.copy(
                                    day = null
                                )
                            } else if (dayInt < 1 || dayInt > 31) {
                                /* 日付が適切でない */
                            } else {
                                newFrequencyInfo = newFrequencyInfo.copy(
                                    day = dayInt
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                }

                Spacer(modifier = Modifier.padding(10.dp))

                Column {
                    Text("Time")
                    TextField(
                        value = newFrequencyInfo.let { info ->
                            if (info.hour != null && info.minute != null) {
                                "%02d:%02d".format(info.hour, info.minute)
                            } else {
                                ""
                            }
                        },
                        onValueChange = {},
                        enabled = false,
                        readOnly = true,
                        modifier = Modifier
                            .width(80.dp)
                            .clickable {
                                isTimePickerVisible = true
                            },
                        colors = enabledTextFiledColorSet()
                    )
                }
            }
        }

        /*******************************************************/
        RepeatFrequency.EVERY_WEEK -> {
            newFrequencyInfo = newFrequencyInfo.copy(
                frequency = RepeatFrequency.EVERY_WEEK,
                month = null,
                day = null
            )
            constSpacer()
            //曜日の指定
            /**********作っていく*******/
            /* これ複数指定したいな、、、 */
            /* チェックボックスを立てに作る */

            //時間の指定
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                /* 曜日のチェックボックスを作る */
                Column {
                    DayOfWeek.entries.forEach { dayOfWeek ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = newFrequencyInfo.dayOfWeek?.contains(dayOfWeek.value)
                                    ?: false,
                                onCheckedChange = { isChecked ->
                                    newFrequencyInfo = newFrequencyInfo.copy(
                                        dayOfWeek = if (isChecked) {
                                            (newFrequencyInfo.dayOfWeek
                                                ?: emptyList()) + dayOfWeek.value
                                        } else {
                                            (newFrequencyInfo.dayOfWeek
                                                ?: emptyList()).filter { it != dayOfWeek.value }
                                        }
                                    )
                                    LogAkitaDebug("newFrequencyInfo: $newFrequencyInfo")
                                }
                            )
                            Text(text = dayOfWeek.label) // ← 修正ポイント
                        }
                    }
                }

                constSpacer()

                Column {
                    Text("Time")
                    TextField(
                        value = newFrequencyInfo.let { info ->
                            if (info.hour != null && info.minute != null) {
                                "%02d:%02d".format(info.hour, info.minute)
                            } else {
                                ""
                            }
                        },
                        onValueChange = {},
                        enabled = false,
                        readOnly = true,
                        modifier = Modifier
                            .width(80.dp)
                            .clickable {
                                isTimePickerVisible = true
                            },
                        colors = enabledTextFiledColorSet()
                    )
                }
            }
        }


        /*******************************************************/
        RepeatFrequency.WEEKDAYS -> {
            newFrequencyInfo = newFrequencyInfo.copy(
                frequency = RepeatFrequency.WEEKDAYS,
                month = null,
                day = null,
                dayOfWeek = null
            )
            constSpacer()
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column {
                    //val tmpFrequencyInfo = newFrequencyInfo
                    Text("Time")
                    TextField(
                        value = newFrequencyInfo.let { info ->
                            if (info.hour != null && info.minute != null) {
                                "%02d:%02d".format(info.hour, info.minute)
                            } else {
                                ""
                            }
                        },
                        onValueChange = {},
                        enabled = false,
                        readOnly = true,
                        modifier = Modifier
                            .width(80.dp)
                            .clickable {
                                isTimePickerVisible = true
                            },
                        colors = enabledTextFiledColorSet()
                    )
                }
            }
        }

        /*******************************************************/
        RepeatFrequency.WEEKENDS -> {
            newFrequencyInfo = newFrequencyInfo.copy(
                frequency = RepeatFrequency.WEEKENDS,
                month = null,
                day = null,
                dayOfWeek = null
            )
            constSpacer()
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column {
                    //val tmpFrequencyInfo = newFrequencyInfo
                    Text("Time")
                    TextField(
                        value = newFrequencyInfo.let { info ->
                            if (info.hour != null && info.minute != null) {
                                "%02d:%02d".format(info.hour, info.minute)
                            } else {
                                ""
                            }
                        },
                        onValueChange = {},
                        enabled = false,
                        readOnly = true,
                        modifier = Modifier
                            .width(80.dp)
                            .clickable {
                                isTimePickerVisible = true
                            },
                        colors = enabledTextFiledColorSet()
                    )
                }
            }
        }

        /*******************************************************/
        RepeatFrequency.EVERYDAY -> {
            newFrequencyInfo = newFrequencyInfo.copy(
                frequency = RepeatFrequency.EVERYDAY,
                month = null,
                day = null,
                dayOfWeek = null
            )
            constSpacer()
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column {
                    //val tmpFrequencyInfo = newFrequencyInfo
                    Text("Time")
                    TextField(
                        value = newFrequencyInfo.let { info ->
                            if (info.hour != null && info.minute != null) {
                                "%02d:%02d".format(info.hour, info.minute)
                            } else {
                                ""
                            }
                        },
                        onValueChange = {},
                        enabled = false,
                        readOnly = true,
                        modifier = Modifier
                            .width(80.dp)
                            .clickable {
                                isTimePickerVisible = true
                            },
                        colors = enabledTextFiledColorSet()
                    )
                }
            }
        }

        else -> {}
    }

    //いろんなところで使っているが、ここに書いておけば良い。
    if (isTimePickerVisible) {
        DialWithDialog(
            onConfirm = { selectedTime ->
                // 選択した時間を取得して ViewModel に更新
                val newTime = LocalTime.of(selectedTime.hour, selectedTime.minute)
                newFrequencyInfo = newFrequencyInfo.copy(
                    hour = newTime.hour,
                    minute = newTime.minute
                )
                isTimePickerVisible = false
            },
            onDismiss = {
                isTimePickerVisible = false
            },
            //@HACK let内に入っているからnullなわけないけど一応気をつけて
            initialDateTime = AppTimeZone.getCurrentTimeInZone().toLocalDate().atTime(time)
        )
    }

    //ここで逐一呼び出し元のfrequencyInfoに代入
    LogAkitaDebug("The end of FrequencyTextField but before callback:${newFrequencyInfo}")
    callback(newFrequencyInfo)
    LogAkitaDebug("The end of FrequencyTextField:newFrequencyInfo:${newFrequencyInfo}")
}

