package gaku.original.myapplication.ui.view.settings

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import gaku.original.myapplication.Utility.getLastDayOfMonth
import gaku.original.myapplication.data.Category
import gaku.original.myapplication.data.Constants.RepeatFrequency
import gaku.original.myapplication.data.Constants.getRepeatFrequencyValues
import gaku.original.myapplication.data.Frequency
import gaku.original.myapplication.data.RepeatAdd
import gaku.original.myapplication.data.defaultRepeatAdd
import gaku.original.myapplication.ui.common.enabledTextFiledColorSet
import gaku.original.myapplication.ui.view.BottomBarView
import gaku.original.myapplication.ui.view.TopBarView
import gaku.original.myapplication.ui.view.main.DialWithDialog
import gaku.original.myapplication.viewModel.RepeatAddViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@Composable
fun RepeatAddSettingView(
    viewModel: RepeatAddViewModel = hiltViewModel(),
    navController: NavController
) {
    val context = LocalContext.current

    var editedRepeatAdd by remember { mutableStateOf(defaultRepeatAdd) }
    var showAddEditDialog by remember { mutableStateOf(false) }

    val allCategories = viewModel.allCategories

    Scaffold(
        topBar = {
            TopBarView("SettingsView作成中")
        },

        bottomBar = { BottomBarView(navController) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.inversePrimary)
            ) {
                Text("ここで検索とかフィルターしたい")
            }
            Button(
                onClick = {
                    showAddEditDialog = true
                }
            ) {
                Text("Show Dialog(Test)")
            }

            if (showAddEditDialog) {
                RepeatAddEditDialog(
                    repeatAdd = editedRepeatAdd,
                    allCategories = allCategories,
                    context = context,
                    onSave = { newRepeatAdd ->
                        //ここに関数を挟んで、
                        showAddEditDialog = false
                        Toast.makeText(
                            context,
                            "Repeat Add Setting を追加したいな",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    onDismiss = {
                        showAddEditDialog = false
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepeatAddEditDialog(
    repeatAdd: RepeatAdd,
    allCategories: StateFlow<List<Category>>,
    context: Context,
    onSave: (repeatAdd: RepeatAdd) -> Unit,
    onDismiss: () -> Unit,
) {
    Log.d("AkitaDebug", "RepeatAddEditDialog Recomposed")
    var newRepeatAdd by remember { mutableStateOf(repeatAdd.copy()) }

    val categories = allCategories.collectAsState()
    var categoryOptionsExpanded by remember { mutableStateOf(false) }
    var amount_warning by remember { mutableStateOf(false) }

    var frequencyOptionsExpanded by remember { mutableStateOf(false) }

    //rememberつけなくてもよいのだが、再コンポーズのたびに関数が呼ばれるのはもったいないので。
    val RepeatFrequencyArray = remember { getRepeatFrequencyValues() }

    LaunchedEffect(amount_warning) {
        //amount_warningは表示したらすぐ消す
        if (amount_warning) {
            val toast = Toast.makeText(
                context,
                "これ以上入力できません。数値が大きすぎます。",
                Toast.LENGTH_LONG
            )
            toast.show()

            delay(2000)

            amount_warning = false
            toast.cancel()
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
                Row() {
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
                                amount_warning = true
                            } else {
                                amount_warning = false
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
                        isError = amount_warning,
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ExposedDropdownMenuBox(
                        expanded = categoryOptionsExpanded,
                        onExpandedChange = {
                            categoryOptionsExpanded = !categoryOptionsExpanded
                        }
                    ) {
                        //カテゴリー(選択肢から選んでもらいたい。RoomDB?)
                        //@Todo タップしたら画面右からスライドして選択肢が入った列が出てくる感じ
                        //とりあえずこれで一応は凌ぐが、本当はもっと使いやすくしたい。

                        /* 設定したcategoryが消えていたらどうしよう.... */
                        TextField(
                            value = newRepeatAdd.expense.category?.name ?: "",
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
                            if (categories.value.isEmpty()) {
                                //何もなかったらToastを出す
                                Log.d("AkitaDebug", "allCategories is empty")
                                categoryOptionsExpanded = false
                            }
                            categories.value.forEachIndexed { index, category ->
                                DropdownMenuItem(
                                    text = { Text(text = category.name.toString()) },
                                    onClick = {
                                        newRepeatAdd = newRepeatAdd.copy(
                                            expense = newRepeatAdd.expense.copy(
                                                category = category
                                            )
                                        )
                                        categoryOptionsExpanded = false
                                    }
                                )

                            }
                        }
                    }

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

                Row() {
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
                            RepeatFrequencyArray.forEachIndexed { index, freq ->
                                DropdownMenuItem(
                                    text = { Text(text = freq.replace("_", " ")) },
                                    onClick = {
                                        newRepeatAdd = newRepeatAdd.copy(
                                            frequencyInfo = newRepeatAdd.frequencyInfo.copy(
                                                frequency = freq
                                            )
                                        )
                                        frequencyOptionsExpanded = false
                                    }
                                )

                            }
                        }
                    }

                    //ここで、選択された内容に応じて表示内容を変える
                    FrequencyTextField(newRepeatAdd.frequencyInfo,
                        callback = { newRepeatAdd = newRepeatAdd.copy(frequencyInfo = it) })
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
                        /* ここでnewRepeatAddが適切かどうかチェックする */
                        val errorMsg = checkNewRepeatAddValid(newRepeatAdd)
                        if (errorMsg == "") {
                            onSave(newRepeatAdd)
                        } else {
                            //エラーをUIに通知する
                            Log.d("AkitaDebug", errorMsg)
                        }
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

    val ConstSpacer = @Composable {
        Spacer(modifier = Modifier.padding(10.dp))
    }

    var day by remember { mutableStateOf<Int?>(null) }
    val time by remember { mutableStateOf(LocalTime.MIDNIGHT) }

    var newFrequencyInfo by remember { mutableStateOf(frequencyInfo) }

    var isTimePickerVisible by remember { mutableStateOf(false) }
    val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

    //Recompositionのたびに上書きする
    newFrequencyInfo = newFrequencyInfo.copy(frequency = frequencyInfo.frequency)
    when (frequencyInfo?.frequency) {
        /*******************************************************/
        RepeatFrequency.EVERY_YEAR -> {
            newFrequencyInfo = newFrequencyInfo.copy(
                frequency = frequencyInfo.frequency,
                dayOfWeek = null //曜日は必要ない
            )
            ConstSpacer()
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column() {
                    Text("Month")
                    TextField(
                        modifier = Modifier.width(50.dp),
                        value = newFrequencyInfo?.month?.toString() ?: "",
                        onValueChange = {
                            val month_int = it.toIntOrNull()
                            /* これ日付がちゃんと存在するかもチェックしたほうが良いな */
                            if (it == "" || month_int == null) {
                                newFrequencyInfo = newFrequencyInfo.copy(
                                    month = null
                                )
                            } else if (month_int > 12 || month_int < 1) {
                                /* Do nothing */
                            } else {
                                newFrequencyInfo = newFrequencyInfo.copy(
                                    month = month_int
                                )
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
                            val day_int = it.toIntOrNull()
                            if (newFrequencyInfo?.month == null) {
                                /* monthを入力してください。snack barをだしたい */
                            } else if (it == "" || day_int == null) {
                                day = null
                                newFrequencyInfo = newFrequencyInfo.copy(
                                    day = null
                                )
                            } else if (day_int < 1 || day_int > getLastDayOfMonth(
                                    year = 2025,/* うるう年でなければ何の年でも良い */
                                    month = newFrequencyInfo?.month
                                        ?: 1/* 上でmonthがnullだったら入力できないようになっているからここでmonthがnullになることはない */
                                ).dayOfMonth
                            ) {
                                /* 日付が適切でない */
                                /* 例えば、31日がない月は30日(月の最終日に追加される) */
                            } else {
                                newFrequencyInfo = newFrequencyInfo.copy(
                                    day = day_int
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
                        } ?: "",
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
            ConstSpacer()
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
                            val day_int = it.toIntOrNull()
                            if (it == "" || day_int == null) {
                                newFrequencyInfo = newFrequencyInfo.copy(
                                    day = null
                                )
                            } else if (day_int < 1 || day_int > 31) {
                                /* 日付が適切でない */
                            } else {
                                newFrequencyInfo = newFrequencyInfo.copy(
                                    day = day_int
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
                        } ?: "",
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
                frequency = RepeatFrequency.EVERY_MONTH,
                month = null,
                day = null
            )
            ConstSpacer()
            //曜日の指定
            /**********作っていく*******/

            //時間の指定
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
                        } ?: "",
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
                frequency = RepeatFrequency.EVERY_MONTH,
                month = null,
                day = null,
                dayOfWeek = null
            )
            ConstSpacer()
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
                        } ?: "",
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
                frequency = RepeatFrequency.EVERY_MONTH,
                month = null,
                day = null,
                dayOfWeek = null
            )
            ConstSpacer()
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
                        } ?: "",
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
                frequency = RepeatFrequency.EVERY_MONTH,
                month = null,
                day = null,
                dayOfWeek = null
            )
            ConstSpacer()
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
                        } ?: "",
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
            initialDateTime = LocalDate.now().atTime(time)
        )
    }

    //ここで逐一呼び出し元のfrequencyInfoに代入
    callback(newFrequencyInfo)
    Log.d("AkitaDebug", "newFrequencyInfo:${newFrequencyInfo}")
}


//ErrorMsgを返したほうが良いのかな？
fun checkNewRepeatAddValid(newRepeatAdd: RepeatAdd): String {
    if (newRepeatAdd.expense.amount == null || newRepeatAdd.expense.amount == 0L) {
        return "expense amount is empty or 0";
    } else if (newRepeatAdd.expense.category == null) {
        return "expense category is empty"
    } else if (newRepeatAdd.frequencyInfo.frequency == null) {
        return "frequency is empty"
    }

    val frequencyInfo = newRepeatAdd.frequencyInfo
    val frequency = frequencyInfo.frequency

    //各頻度ごとに該当するフィールドのチェックを追加
    if (frequency == RepeatFrequency.EVERY_YEAR) {
        if (frequencyInfo.month == null) return "month is empty"
    }

    if (frequency == RepeatFrequency.EVERY_YEAR ||
        frequency == RepeatFrequency.EVERY_MONTH
    ) {
        if (frequencyInfo.day == null) return "day is empty"
    }

    if (frequency == RepeatFrequency.EVERY_WEEK) {
        if (frequencyInfo.dayOfWeek == null) return "day of week is empty"
    }

    if (frequency == RepeatFrequency.EVERY_YEAR ||
        frequency == RepeatFrequency.EVERY_MONTH ||
        frequency == RepeatFrequency.EVERY_WEEK ||
        frequency == RepeatFrequency.WEEKENDS ||
        frequency == RepeatFrequency.WEEKDAYS ||
        frequency == RepeatFrequency.EVERYDAY
    ) {
        if (frequencyInfo.hour == null) return "hour is empty"
        if (frequencyInfo.minute == null) return "minute is empty"
    }

    return ""
}