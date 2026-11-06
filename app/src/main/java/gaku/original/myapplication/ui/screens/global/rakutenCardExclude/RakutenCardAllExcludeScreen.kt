package gaku.original.myapplication.ui.screens.global.rakutenCardExclude

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.data.dataClass.MatchCondition
import gaku.original.myapplication.data.dataClass.RakutenCardExcludeItem
import gaku.original.myapplication.ui.common.ConfirmAlertDialog
import gaku.original.myapplication.ui.common.DialogCard
import gaku.original.myapplication.ui.common.EmptyState
import gaku.original.myapplication.ui.common.LoadingBox
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.ui.common.enabledTextFiledColorSet
import gaku.original.myapplication.ui.screens.global.categoryAssignment.editDialog.toDisplayName

@Composable
fun RakutenCardAllExcludeScreenRoot(
    navHostController: NavHostController,
    viewModel: RakutenCardAllExcludeViewModel = viewModel(factory = RakutenCardAllExcludeViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val screenSnackbarHostState = LocalSnackBarHostState.current
    /* A snackbar on the screen would be hidden behind the open dialog. */
    val dialogSnackbarHostState = remember { SnackbarHostState() }

    var itemToDelete by remember { mutableStateOf<RakutenCardExcludeItem?>(null) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            val hostState =
                if (uiState.isAddDialogShown) dialogSnackbarHostState else screenSnackbarHostState
            hostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    RakutenCardAllExcludeScreen(
        uiState = uiState,
        snackbarHostState = screenSnackbarHostState,
        onBackNavClick = {
            navHostController.popBackStack()
        },
        onAddClick = {
            viewModel.onAddClick()
        },
        onDeleteClick = {
            itemToDelete = it
        }
    )

    if (uiState.isAddDialogShown) {
        Dialog(
            onDismissRequest = {
                viewModel.onAddDialogDismiss()
            }
        ) {
            RakutenCardAllExcludeAddDialog(
                uiState = uiState,
                snackbarHostState = dialogSnackbarHostState,
                onNameChange = {
                    viewModel.onNameChange(it)
                },
                onConditionSelected = {
                    viewModel.onConditionSelected(it)
                },
                onSaveClick = {
                    viewModel.onSaveClick()
                },
                onCancelClick = {
                    viewModel.onAddDialogDismiss()
                }
            )
        }
    }

    itemToDelete?.let { item ->
        ConfirmAlertDialog(
            onDismissRequest = {
                itemToDelete = null
            },
            onClick = {
                viewModel.onDeleteClick(item)
                itemToDelete = null
            }
        ) {
            Text("Are you sure to Delete?")
        }
    }
}

@Composable
fun RakutenCardAllExcludeScreen(
    uiState: RakutenCardAllExcludeUiState,
    snackbarHostState: SnackbarHostState,
    onBackNavClick: () -> Unit,
    onAddClick: () -> Unit,
    onDeleteClick: (RakutenCardExcludeItem) -> Unit
) {
    Scaffold(
        topBar = {
            TopBarView(
                title = "Excluded Stores",
                showBackButton = true,
                onBackNavClicked = {
                    onBackNavClick()
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = {
                    onAddClick()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                LoadingBox()
            } else if (uiState.items.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Block,
                    title = "No excluded stores",
                    message = "Rakuten Card charges at the stores you add here aren't saved as expenses."
                )
            } else {
                Text(
                    text = "Rakuten Card charges at these stores aren't saved as expenses.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.items, key = { it.item.id ?: "" }) { itemUi ->
                        ExcludeItemCard(
                            itemUi = itemUi,
                            onDeleteClick = onDeleteClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExcludeItemCard(
    itemUi: ExcludeItemUiState,
    onDeleteClick: (RakutenCardExcludeItem) -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = itemUi.item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = itemUi.item.condition.toDisplayName(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (itemUi.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(12.dp)
                        .size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                IconButton(
                    onClick = {
                        onDeleteClick(itemUi.item)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RakutenCardAllExcludeAddDialog(
    uiState: RakutenCardAllExcludeUiState,
    snackbarHostState: SnackbarHostState,
    onNameChange: (String) -> Unit,
    onConditionSelected: (MatchCondition) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    var conditionExpanded by rememberSaveable { mutableStateOf(false) }

    DialogCard(
        title = "New Excluded Store",
        snackbarHostState = snackbarHostState,
        actions = {
            TextButton(
                onClick = {
                    onCancelClick()
                },
                enabled = !uiState.isSaving
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    onSaveClick()
                }
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save")
                }
            }
        }
    ) {
        Text(
            text = "Enter the store name as written in the Rakuten Card mail. Only mails that arrive after saving are affected.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.name,
            singleLine = true,
            onValueChange = {
                onNameChange(it)
            },
            label = {
                Text("Store name")
            }
        )

        Box {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = {
                            conditionExpanded = true
                        }
                    ),
                value = uiState.condition.toDisplayName(),
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = {
                    Text("Condition")
                },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                colors = enabledTextFiledColorSet()
            )
            DropdownMenu(
                expanded = conditionExpanded,
                onDismissRequest = {
                    conditionExpanded = false
                }
            ) {
                MatchCondition.entries.forEach {
                    DropdownMenuItem(
                        text = {
                            Text(it.toDisplayName())
                        },
                        onClick = {
                            onConditionSelected(it)
                            conditionExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RakutenCardAllExcludeScreenPreview() {
    RakutenCardAllExcludeScreen(
        uiState = RakutenCardAllExcludeUiState(
            items = listOf(
                ExcludeItemUiState(
                    item = RakutenCardExcludeItem(
                        id = "1",
                        name = "テスト店",
                        condition = MatchCondition.EXACT
                    )
                ),
                ExcludeItemUiState(
                    isLoading = true,
                    item = RakutenCardExcludeItem(
                        id = "2",
                        name = "テスト",
                        condition = MatchCondition.CONTAINS
                    )
                )
            )
        ),
        snackbarHostState = SnackbarHostState(),
        onBackNavClick = {},
        onAddClick = {},
        onDeleteClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun RakutenCardAllExcludeAddDialogPreview() {
    RakutenCardAllExcludeAddDialog(
        uiState = RakutenCardAllExcludeUiState(isAddDialogShown = true, name = "テスト店"),
        snackbarHostState = SnackbarHostState(),
        onNameChange = {},
        onConditionSelected = {},
        onSaveClick = {},
        onCancelClick = {}
    )
}
