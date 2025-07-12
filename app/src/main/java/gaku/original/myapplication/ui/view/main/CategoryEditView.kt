package gaku.original.myapplication.ui.view.main

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import gaku.original.myapplication.R
import gaku.original.myapplication.data.Constants.CATEGORY_NULL_REPLACEMENT
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.ui.common.BottomBarView
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.viewModel.main.CategoryEditViewModel
import kotlinx.coroutines.launch

@Composable
fun CategoryAddEditView(
    viewModel: CategoryEditViewModel = hiltViewModel(),

    navController: NavController
) {
    var editedCategory by remember { mutableStateOf(Category(name = null)) }
    var showDialog by remember { mutableStateOf(false) }
    var removeShowDialog by remember { mutableStateOf(false) }

    val allCategories by remember { viewModel.allCategories }.collectAsState(initial = emptyList())

    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    val snackBarHostState = remember {
        SnackbarHostState()
    }
    Scaffold(
        topBar = {
            TopBarView(
                title = "What is essential is invisible to the eye",
                onBackNavClicked = {
                    navController.popBackStack()
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        bottomBar = { BottomBarView(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(allCategories) {
                    CategoryItem(
                        category = it,
                        onClick = {
                            showDialog = true
                            editedCategory = it
                        },
                        onDelete = {
                            removeShowDialog = true
                            editedCategory = it
                        }
                    )
                }
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    showDialog = true
                    editedCategory = Category(
                        name = null
                    )
                }
            ) {
                Text("Add Category")
            }

            //ダイアログを表示
            if (showDialog) {
                //EditかAddはeditedCategoryのidがnullかどうかで判断する
                CategoryAddEditDialog(
                    category = editedCategory,
                    onSave = { newCategory ->
                        if (newCategory.id == null) {
                            //新規追加
                            viewModel.addCategory(
                                newCategory,
                                callback = { status ->
                                    when (status.status) {
                                        SuspendFuncStatus.SUCCESS -> {
                                            Toast.makeText(
                                                context,
                                                "カテゴリーを追加しました",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            showDialog = false
                                        }

                                        SuspendFuncStatus.TIMEOUT -> {
                                            Toast.makeText(
                                                context,
                                                status.errorMessage,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            showDialog = false
                                        }

                                        SuspendFuncStatus.FAILED -> {
                                            Toast.makeText(
                                                context,
                                                status.errorMessage,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            showDialog = false
                                        }
                                    }

                                }
                            )
                        } else {
                            //編集
                            viewModel.updateCategory(
                                newCategory,

                                callback = { status ->
                                    when (status.status) {
                                        SuspendFuncStatus.SUCCESS -> {
                                            showDialog = false
                                            Toast.makeText(
                                                context,
                                                "カテゴリーを編集しました",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        SuspendFuncStatus.TIMEOUT -> {
                                            Toast.makeText(
                                                context,
                                                status.errorMessage,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            showDialog = false
                                        }

                                        SuspendFuncStatus.FAILED -> {
                                            Toast.makeText(
                                                context,
                                                status.errorMessage,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            showDialog = false
                                        }
                                    }

                                }
                            )
                        }
                    },
                    onDismiss = {
                        showDialog = false
                    }
                )
            }

            if (removeShowDialog) {
                CategoryRemoveConfirmDialog(
                    category = editedCategory,
                    onOK = { categoryRemoved ->
                        viewModel.removeCategory(
                            category = categoryRemoved,
                            callback = { status ->
                                if (status.status != SuspendFuncStatus.SUCCESS) {
                                    scope.launch {
                                        snackBarHostState.showSnackbar("${status.errorMessage}:${categoryRemoved.name}")
                                    }
                                }
                            }
                        )
                        removeShowDialog = false
                    },
                    onDismiss = {
                        removeShowDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    onClick: (category: Category) -> Unit = {},
    onDelete: (category: Category) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick(category)
            }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        //nullになることは基本的にない
        Text(modifier = Modifier.weight(1f), text = category.name ?: CATEGORY_NULL_REPLACEMENT)

        // ゴミ箱ボタン
        IconButton(
            onClick = { onDelete(category) }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_delete_24), // カスタムアイコン
                contentDescription = "Delete Category"
            )
        }
    }
}

@Composable
fun CategoryAddEditDialog(
    category: Category,
    onSave: (category: Category) -> Unit,
    onDismiss: () -> Unit,
) {
    var newCategory by remember { mutableStateOf(category) }

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
                        /* ここでnewCategoryが適切かチェックする */
                        /* すでにカテゴリーの中に存在するかはここではチェックしない */
                        if (newCategory.name == null || newCategory.name == "") {
                            /* 何もしないか、Toastをだす */
                        } else if (newCategory.name == category.name) {
                            /* 編集だけど何も変わっていない場合 */
                            onDismiss()
                        } else {
                            onSave(newCategory)
                        }
                    }
                ) {
                    Text("Save")
                }
            }
        },
        title = {
            if (newCategory.id == null) {
                Text("Add Category")
            } else {
                Text("Edit Category")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = newCategory.name ?: "",
                    onValueChange = {
                        newCategory = newCategory.copy(name = it)
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

@Composable
fun CategoryRemoveConfirmDialog(
    category: Category,
    onOK: (category: Category) -> Unit,
    onDismiss: () -> Unit,
) {
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
                        onOK(category)
                    }
                ) {
                    Text("OK")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Are you sure you want to remove the category?")
                Text("****************************")
                Text("${category.name}")
                Text("****************************")
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = true
        )
    )
}