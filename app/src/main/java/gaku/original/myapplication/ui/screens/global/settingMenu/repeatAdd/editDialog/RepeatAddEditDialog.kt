package gaku.original.myapplication.ui.screens.global.settingMenu.repeatAdd.editDialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.data.dataClass.InitialCategories.categories
import gaku.original.myapplication.ui.common.CategoryDropDown

@Composable
fun RepeatAddEditDialogRoot(
    navHostController: NavHostController,
    viewModel: RepeatAddEditViewModel = viewModel(factory = RepeatAddEditViewModel.Factory(null))
) {
    val uiState by viewModel.uiState.collectAsState()

    RepeatAddEditDialog(
        uiState
    )
}

@Composable
fun RepeatAddEditDialog(
    uiState: RepeatAddEditDialogState
) {
    var frequencyExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(
                    alpha = 0.6f
                )
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary
            ),
        verticalArrangement = Center
    ) {
        TextField(
            value = "${uiState.amount ?: ""}",
            onValueChange = {

            },
            label = { Text("Amount(yen)") }
        )

        CategoryDropDown(
            initialCategory = uiState.category,
            categories = categories,
            onCategorySelected = {

            }
        )

        TextField(
            value = uiState.note ?: "",
            onValueChange = {

            },
            label = { Text("Note") }
        )

        TextField(
            value = uiState.itemName ?: "",
            onValueChange = {},
            label = { Text("Item name") }
        )

        TextField(
            value = uiState.storeName ?: "",
            onValueChange = {},
            label = { Text("Store name") }
        )

        Row {
            Text("Frequency")
        }

        //DropdownMenu() { }
        DropdownMenu(
            expanded = frequencyExpanded,
            onDismissRequest = {
                frequencyExpanded = false
            }
        ) {

        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Button(
                onClick = {}
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {}
            ) {
                Text("Save")
            }
        }
    }
}

@Preview
@Composable
fun RepeatAddEditDialogPreview() {
    val uiState = RepeatAddEditDialogState()

    RepeatAddEditDialog(
        uiState
    )
}