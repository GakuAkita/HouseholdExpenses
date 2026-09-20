package gaku.original.myapplication.ui.screens.global.categoryAssignment.editDialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import gaku.original.myapplication.data.dataClass.MatchCondition
import gaku.original.myapplication.ui.common.CategoryDropDown
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
    val FieldWidth = 300.dp

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .background(MaterialTheme.colorScheme.onSecondary)
                .verticalScroll(
                    rememberScrollState()
                ),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Assign category automatically when the name is found in product name or store name",
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Box {
                TextField(
                    modifier = Modifier
                        .width(FieldWidth)
                        .padding(4.dp)
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

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    modifier = Modifier
                        .widthIn(300.dp)
                        .padding(start = 4.dp),
                    value = uiState.name ?: "",
                    singleLine = true,
                    onValueChange = {
                        onNameChange(it)
                    },
                    label = {
                        Text("Name")
                    }
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    //modifier = Modifier.padding(4.dp),
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
                        .width(FieldWidth)
                        .padding(horizontal = 4.dp, vertical = 8.dp)
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
                    colors = enabledTextFiledColorSet()
                )
                /* Product name or Store name */
                DropdownMenu(
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
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
                modifier = Modifier
                    .width(FieldWidth)
                    .padding(4.dp),
                selectedCategoryId = uiState.categoryId,
                categories = uiState.categories,
                onCategorySelected = {
                    onCategorySelected(it.id)
                },
                nullOption = true
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    modifier = Modifier.width(100.dp),
                    onClick = {
                        if (uiState.isLoading) {
                            return@Button
                        }
                        onCancelClick()
                    },
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Cancel")
                    }
                }

                Button(
                    modifier = Modifier.width(100.dp),
                    onClick = {
                        if (uiState.isLoading) {
                            return@Button
                        }
                        onSaveClick()
                    },
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Save")
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
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