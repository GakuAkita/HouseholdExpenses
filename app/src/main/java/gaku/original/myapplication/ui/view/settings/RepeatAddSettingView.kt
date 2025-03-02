package gaku.original.myapplication.ui.view.settings

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import gaku.original.myapplication.data.Category
import gaku.original.myapplication.data.RepeatAdd
import gaku.original.myapplication.data.defaultRepeatAdd
import gaku.original.myapplication.ui.common.enabledTextFiledColorSet
import gaku.original.myapplication.ui.view.BottomBarView
import gaku.original.myapplication.ui.view.TopBarView
import gaku.original.myapplication.viewModel.RepeatAddViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow


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
    Log.d("AkitaDebug", "Recomposed??")
    var newRepeatAdd by remember { mutableStateOf(repeatAdd.copy()) }
    Log.d("AkitaDebug", "newRepeatAdd :${newRepeatAdd}")

    val categories = allCategories.collectAsState()
    var categoryOptionsExpanded by remember { mutableStateOf(false) }
    var amount_warning by remember { mutableStateOf(false) }

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
                horizontalAlignment = Alignment.CenterHorizontally
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
                        value = newRepeatAdd.expense?.amount?.toString() ?: "",
                        onValueChange = {
                            if (it != "" && it.toLongOrNull() == null) {
                                /* Do nothing */
                                amount_warning = true
                            } else {
                                amount_warning = false
                                newRepeatAdd = newRepeatAdd.copy(
                                    expense = newRepeatAdd.expense?.copy(
                                        amount = it.toLongOrNull()
                                    )
                                )
                            }
                        },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = enabledTextFiledColorSet(),
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
                        //カテゴリーの編集画面もほしいし
                        TextField(
                            value = newRepeatAdd.expense?.category?.name ?: "",
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
                                            expense = newRepeatAdd.expense?.copy(
                                                category = category
                                            )
                                        )
                                        categoryOptionsExpanded = false
                                    }
                                )

                            }
                        }
                    }
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
                ) {

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
                        if (newRepeatAdd.expense == null) {
                            /* Toast出す */
                        } else if (newRepeatAdd.frequency == null) {
                            /* Toast出す */
                        } else {
                            onSave(newRepeatAdd)
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