package gaku.original.myapplication.ui.screens.global.categoryAssignment.editDialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import gaku.original.myapplication.MainGraph
import gaku.original.myapplication.data.dataClass.MatchCondition
import gaku.original.myapplication.ui.common.CategoryDropDown
import gaku.original.myapplication.ui.common.DialogCard
import gaku.original.myapplication.ui.common.enabledTextFiledColorSet

@Composable
fun CategoryAssignmentEditDialogRoot(
    viewModel: CategoryAssignmentEditViewModel,
    navHostController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = SnackbarHostState()
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it, actionLabel = "OK")
            viewModel.onMessageShown()
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            // https://developer.android.com/guide/navigation/navigation-3/migration-guide?authuser=102&utm_source=chatgpt.com#result-migration
            navHostController
                .previousBackStackEntry
                ?.savedStateHandle
                ?.set(MainGraph.Global.ICategoryAssignment.EditDialog.KEY_UPDATED, true)
            navHostController.popBackStack()
        }
    }

    CategoryAssignmentEditDialog(
        uiState = uiState,
        snackbarHostState,
        onTypeSelected = {
            viewModel.onTypeSelected(it)
        },
        onNameChange = {
            viewModel.onNameChange(it)
        },
        onRegexClick = {
            viewModel.onRegexClick()
        },
        onMatchConditionSelected = {
            viewModel.onMatchConditionSelected(it)
        },
        onCategorySelected = {
            viewModel.onCategorySelected(it)
        },
        onSaveClick = {
            viewModel.onSaveClick()
        },
        onCancelClick = {
            navHostController.popBackStack()
        }
    )
}

@Composable
fun CategoryAssignmentEditDialog(
    uiState: CategoryAssignmentEditUiState,
    snackbarHostState: SnackbarHostState,
    onTypeSelected: (CategoryAssignmentType) -> Unit,
    onNameChange: (String) -> Unit,
    onRegexClick: () -> Unit,
    onMatchConditionSelected: (MatchCondition) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    var typeExpanded by rememberSaveable { mutableStateOf(false) }
    var matchConditionExpanded by rememberSaveable { mutableStateOf(false) }

    val typeDescription = remember {
        if (uiState.isEdit) {
            when (uiState.type) {
                CategoryAssignmentType.PRODUCT -> "product name"
                CategoryAssignmentType.STORE -> "store name"
                null -> "???"
            }
        } else {
            "product name or store name"
        }
    }

    DialogCard(
        title = if (uiState.isEdit) "Edit Assignment" else "New Assignment",
        snackbarHostState = snackbarHostState,
        actions = {
            TextButton(
                onClick = {
                    if (uiState.isLoading) {
                        return@TextButton
                    }
                    onCancelClick()
                },
                enabled = !uiState.isLoading
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    if (uiState.isLoading) {
                        return@Button
                    }
                    onSaveClick()
                }
            ) {
                if (uiState.isLoading) {
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
            text = "Assign category automatically when the name is found in $typeDescription",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!uiState.isEdit) {
            Box {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            onClick = {
                                typeExpanded = true
                            }
                        ),
                    value = uiState.type?.toDisplayName() ?: "",
                    onValueChange = {},
                    label = {
                        Text("Assignment Type")
                    },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                    readOnly = true,
                    enabled = false,
                    colors = enabledTextFiledColorSet(),
                )
                DropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = {
                        typeExpanded = false
                    }
                ) {
                    CategoryAssignmentType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = {
                                Text(type.toDisplayName())
                            },
                            onClick = {
                                onTypeSelected(type)
                                typeExpanded = false
                            }
                        )
                    }
                }
            }
        }

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.name ?: "",
            singleLine = true,
            onValueChange = {
                onNameChange(it)
            },
            label = {
                Text("Name")
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onRegexClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = uiState.isRegex,
                onCheckedChange = {
                    onRegexClick()
                }
            )

            Text("Regular Expression")
        }

        Box {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = {
                            matchConditionExpanded = true
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
            /* Product name or Store name */
            DropdownMenu(
                expanded = matchConditionExpanded,
                onDismissRequest = {
                    matchConditionExpanded = false
                }
            ) {
                MatchCondition.entries.forEach {
                    DropdownMenuItem(
                        text = {
                            Text(it.toDisplayName())
                        },
                        onClick = {
                            onMatchConditionSelected(it)
                            matchConditionExpanded = false
                        }
                    )
                }
            }
        }

        CategoryDropDown(
            modifier = Modifier.fillMaxWidth(),
            selectedCategoryId = uiState.categoryId,
            categories = uiState.categories,
            onCategoryIdSelected = {
                onCategorySelected(it)
            },
            nullOption = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryAssignmentEditDialogPreview() {
    val uiState = CategoryAssignmentEditUiState(
        isLoading = true
    )

    CategoryAssignmentEditDialog(
        uiState,
        SnackbarHostState(),
        onTypeSelected = {},
        onNameChange = {},
        onRegexClick = {},
        onMatchConditionSelected = {},
        onCategorySelected = {},
        onSaveClick = {},
        onCancelClick = {}
    )
}

fun MatchCondition.toDisplayName(): String =
    when (this) {
        MatchCondition.EXACT -> "Exact Match"
        MatchCondition.CONTAINS -> "Contains"
    }

fun CategoryAssignmentType.toDisplayName(): String =
    when (this) {
        CategoryAssignmentType.PRODUCT -> "Assign by product name"
        CategoryAssignmentType.STORE -> "Assign by store name"
    }