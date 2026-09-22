package gaku.original.myapplication.ui.screens.global.settingMenu.repeatAdd.executeDialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun RepeatAddExecuteDialogRoot(
    viewModel: RepeatAddExecuteViewModel
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RepeatAddExecuteDialog(
        uiState
    )
}

@Composable
fun RepeatAddExecuteDialog(
    uiState: RepeatAddExecuteUiState
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
    }
}

@Preview
@Composable
fun RepeatAddExecuteDialogPreview() {
    val uiState = RepeatAddExecuteUiState()
    RepeatAddExecuteDialog(
        uiState
    )
}