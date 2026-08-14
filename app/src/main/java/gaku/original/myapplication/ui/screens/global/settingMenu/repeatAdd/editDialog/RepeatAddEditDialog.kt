package gaku.original.myapplication.ui.screens.global.settingMenu.repeatAdd.editDialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(
                    alpha = 0.6f
                )
            ).border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary
            ),
        verticalArrangement = Center
    ) {
        Text("aa")
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