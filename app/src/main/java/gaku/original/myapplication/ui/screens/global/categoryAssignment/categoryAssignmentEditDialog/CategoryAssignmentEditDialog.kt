package gaku.original.myapplication.ui.screens.global.categoryAssignment.categoryAssignmentEditDialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import gaku.original.myapplication.LocalSnackBarHostState

@Composable
fun CategoryAssignmentEditDialogRoot(
    viewModel: CategoryAssignmentEditViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = LocalSnackBarHostState.current
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    CategoryAssignmentEditDialog(
        uiState = uiState,
    )
}

@Composable
fun CategoryAssignmentEditDialog(
    uiState: CategoryAssignmentEditUiState
) {

}

@Preview
@Composable
fun CategoryAssignmentEditDialogPreview() {

}