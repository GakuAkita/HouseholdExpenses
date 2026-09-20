package gaku.original.myapplication.ui.screens.global.categoryAssignment.editDialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
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
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.data.dataClass.MatchCondition
import gaku.original.myapplication.ui.common.CategoryDropDown
import gaku.original.myapplication.ui.common.enabledTextFiledColorSet

@Composable
fun CategoryAssignmentEditDialogRoot(
    viewModel: CategoryAssignmentEditViewModel,
    navHostController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = LocalSnackBarHostState.current
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it, actionLabel = "OK")
            viewModel.onMessageShown()
        }
    }

    CategoryAssignmentEditDialog(
        uiState = uiState,
        snackbarHostState,
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
    onNameChange: (String) -> Unit,
    onRegexClick: () -> Unit,
    onMatchConditionSelected: (MatchCondition) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    var matchConditionExpanded by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .background(MaterialTheme.colorScheme.onSecondary),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Assign category automatically when the name is found in product name or store name",
                modifier = Modifier.padding(horizontal = 4.dp)
            )

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
                        .width(300.dp)
                        .padding(4.dp)
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
                    .width(300.dp)
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
                    onClick = {
                        onCancelClick()
                    },
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        onSaveClick()
                    },
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Save")
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

    )

    CategoryAssignmentEditDialog(
        uiState,
        SnackbarHostState(),
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