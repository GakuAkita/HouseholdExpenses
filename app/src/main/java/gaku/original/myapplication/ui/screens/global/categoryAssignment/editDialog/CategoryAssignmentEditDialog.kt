package gaku.original.myapplication.ui.screens.global.categoryAssignment.editDialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.data.dataClass.MatchCondition
import gaku.original.myapplication.ui.common.CategoryDropDown
import gaku.original.myapplication.ui.common.enabledTextFiledColorSet

@Composable
fun CategoryAssignmentEditDialogRoot(
    viewModel: CategoryAssignmentEditViewModel
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
        }
    )
}

@Composable
fun CategoryAssignmentEditDialog(
    uiState: CategoryAssignmentEditUiState,
    snackbarHostState: SnackbarHostState,
    onNameChange: (String) -> Unit
) {

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
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
                        .width(250.dp)
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
                Column(
                    modifier = Modifier.wrapContentWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(-12.dp)
                ) {
                    Text("Regex")
                    Checkbox(
                        checked = uiState.isRegex,
                        onCheckedChange = {

                        }
                    )
                }
            }

            Box {
                TextField(
                    modifier = Modifier
                        .width(300.dp)
                        .padding(4.dp),
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
                    expanded = uiState.conditionExpanded,
                    onDismissRequest = {}
                ) {
                    MatchCondition.entries.forEach {
                        DropdownMenuItem(
                            text = {
                                Text(it.toDisplayName())
                            },
                            onClick = {
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
                },
                nullOption = true
            )
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
        onNameChange = {}
    )
}

fun MatchCondition.toDisplayName(): String =
    when (this) {
        MatchCondition.EXACT -> "Exact Match"
        MatchCondition.CONTAINS -> "Contains"
    }