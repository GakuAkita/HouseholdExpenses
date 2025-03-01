package gaku.original.myapplication.ui.view.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import gaku.original.myapplication.data.RepeatAdd
import gaku.original.myapplication.data.defaultRepeatAdd
import gaku.original.myapplication.ui.view.BottomBarView
import gaku.original.myapplication.ui.view.TopBarView
import gaku.original.myapplication.viewModel.RepeatAddViewModel


@Composable
fun RepeatAddSettingView(
    viewModel: RepeatAddViewModel = hiltViewModel(),
    navController: NavController
) {
    var editedRepeatAdd by remember { mutableStateOf(defaultRepeatAdd) }
    var showAddEditDialog by remember { mutableStateOf<Boolean>(false) }

    Scaffold(
        topBar = {
            TopBarView("SettingsView作成中")
        },

        bottomBar = { BottomBarView(navController) }
    ) { innerPadding ->
        val context = LocalContext.current

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
                    onSave = { newRepeatAdd ->
                        Toast.makeText(
                            context,
                            "数値が大きすぎます。これ以上入力できません",
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


@Composable
fun RepeatAddEditDialog(
    repeatAdd: RepeatAdd,
    onSave: (repeatAdd: RepeatAdd) -> Unit,
    onDismiss: () -> Unit,
) {
    var newRepeatAdd by remember { mutableStateOf(repeatAdd) }

    AlertDialog(
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
                    onClick = { onDismiss() }
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
        title = {
            if (repeatAdd.id == null) {
                Text("Add RepeatAdd Setting")
            } else {
                Text("Edit RepeatAdd Setting")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                //選択肢にしたい...
                OutlinedTextField(
                    value = repeatAdd.frequency ?: "",
                    onValueChange = {
                        newRepeatAdd = newRepeatAdd.copy(frequency = it)
                    },
                    singleLine = true
                )
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = true
        )
    )
}
