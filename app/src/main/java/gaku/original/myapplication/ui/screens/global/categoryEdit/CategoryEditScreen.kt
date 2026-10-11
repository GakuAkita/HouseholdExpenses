package gaku.original.myapplication.ui.screens.global.categoryEdit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.R
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.ui.common.EmptyState
import gaku.original.myapplication.ui.common.FloatingActionButtonWithIcon
import gaku.original.myapplication.ui.common.TopBarView

@Composable
fun CategoryEditScreenRoot(
    navHostController: NavHostController,
    viewModel: CategoryEditViewModel = viewModel(factory = CategoryEditViewModel.Factory)
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackBarHostState.current

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it, actionLabel = "OK")
        }
    }

    CategoryEditScreen(
        uiState,
        snackbarHostState,
        onBackNavClicked = { navHostController.popBackStack() },
        onCategorySelected = {
            viewModel.onCategorySelected(it)
        },
        onCategoryAddClick = {
            viewModel.onCategoryAddClick()
        },
        onEditDialogDismiss = {
            viewModel.closeEditDialog()
        },
        onDeleteDialogDismiss = {
            viewModel.closeDeleteDialog()
        },
        onSaveClick = {
            viewModel.onSave(it)
        },
        onDeleteClick = {
            viewModel.onDelete(it)
        },
        onDeleteIconClick = {
            viewModel.onDeleteIconClick(it)
        },
        onCategoryNameChange = {
            viewModel.eraseDialogMessage()
        }
    )
}

@Composable
fun CategoryEditScreen(
    uiState: CategoryEditUiState,
    snackbarHostState: SnackbarHostState,
    onBackNavClicked: () -> Unit,
    onCategorySelected: (Category) -> Unit,
    onDeleteIconClick: (Category) -> Unit,
    onCategoryAddClick: () -> Unit,
    onEditDialogDismiss: () -> Unit,
    onDeleteDialogDismiss: () -> Unit,
    onSaveClick: (name: String) -> Unit,
    onDeleteClick: (Category) -> Unit,
    onCategoryNameChange: (String) -> Unit/* used to change erase the dialog message */
) {
    Scaffold(
        topBar = {
            TopBarView(
                "Categories",
                showBackButton = true,
                onBackNavClicked = {
                    onBackNavClicked()
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButtonWithIcon(
                onClick = {
                    onCategoryAddClick()
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.categories.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Category,
                    title = "No categories yet",
                    message = "Tap the + button to add your first category."
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.categories) {
                    CategoryItem(
                        category = it,
                        onClick = {
                            onCategorySelected(it)
                        },
                        onDelete = {
                            onDeleteIconClick(it)
                        }
                    )
                }

                /* Move to Floating Action button */
//                item(span = { GridItemSpan(maxLineSpan) }) {
//                    Button(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 100.dp, vertical = 16.dp),
//                        onClick = {
//                            onCategoryAddClick()
//                        }
//                    ) {
//                        Text("Add Category")
//                    }
//                }
            }

            if (uiState.isShowEditDialog) {
                CategoryAddEditDialog(
                    category = uiState.selectedCategory,
                    onSave = {
                        onSaveClick(it)
                    },
                    onDismiss = {
                        onEditDialogDismiss()
                    },
                    errorMessage = uiState.messageInDialog,
                    onValueChange = {
                        /* erase the message in the dialog */
                        onCategoryNameChange(it)
                    }
                )
            }

            if (uiState.isDeleteShowDialog) {
                CategoryRemoveConfirmDialog(
                    category = uiState.selectedCategory!!,
                    onOK = {
                        onDeleteClick(it)
                    },
                    onDismiss = {
                        onDeleteDialogDismiss()
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun CategoryEditScreenPreview() {
    val uiState = CategoryEditUiState(
        categories = listOf(
            Category(
                id = "1",
                timestamp = 0L,
                name = "Food",
                enabled = true
            ),
            Category(
                id = "2",
                timestamp = 0L,
                name = "Waste",
                enabled = true
            ),
            Category(
                id = "3",
                timestamp = 0L,
                name = "aa.bbbbbbccccccccccddddddd",
                enabled = true
            ),
            Category(
                id = "4",
                timestamp = 0L,
                name = "bb",
                enabled = true
            )
        )
    )

    CategoryEditScreen(
        uiState,
        SnackbarHostState(),
        onBackNavClicked = {},
        onCategorySelected = {},
        onDeleteIconClick = {},
        onCategoryAddClick = {},
        onEditDialogDismiss = {},
        onDeleteDialogDismiss = {},
        onSaveClick = {},
        onDeleteClick = {},
        onCategoryNameChange = {}
    )
}

@Composable
fun CategoryItem(
    category: Category,
    onClick: (category: Category) -> Unit = {},
    onDelete: (category: Category) -> Unit = {}
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        onClick = { onClick(category) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.name.take(1),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            //nullになることは基本的にない
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                text = category.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // ゴミ箱ボタン
            IconButton(
                onClick = { onDelete(category) }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_delete_24), // カスタムアイコン
                    contentDescription = "Delete Category",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CategoryAddEditDialog(
    category: Category?, /* null when adding */
    onSave: (name: String) -> Unit,
    onDismiss: () -> Unit,
    errorMessage: String? = null,
    onValueChange: (String) -> Unit = {}
) {
    var name by remember { mutableStateOf(category?.name ?: "") }

    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotEmpty(),
                onClick = {
                    /* ここでnameが適切かチェックする */
                    /* すでにカテゴリーの中に存在するかはここではチェックしない */
                    if (name == "") {
                        /* 何もしないか、Toastをだす */
                    } else if (name == category?.name) {
                        /* 編集だけど何も変わっていない場合 */
                        onDismiss()
                    } else {
                        onSave(name)
                    }
                }
            ) {
                Text("Save")
            }
        },
        title = {
            if (category == null) {
                Text("Add Category")
            } else {
                Text("Edit Category")
            }
        },
        text = {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = name,
                onValueChange = {
                    onValueChange(it)
                    name = it
                },
                label = { Text("Name") },
                isError = errorMessage != null,
                supportingText = errorMessage?.let { { Text(it) } },
                singleLine = true
            )
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
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.baseline_delete_24),
                contentDescription = null
            )
        },
        title = {
            Text("Remove category?")
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onOK(category)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Remove")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Are you sure you want to remove the category?")
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = true
        )
    )
}
