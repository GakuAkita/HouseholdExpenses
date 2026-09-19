package gaku.original.myapplication.ui.screens.global.categoryAssignment.editDialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import gaku.original.myapplication.ui.common.CategoryDropDown

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


            Text("Assign category")
            TextField(
                modifier = Modifier
                    .width(280.dp)
                    .padding(4.dp),
                value = uiState.name ?: "",
                singleLine = true,
                onValueChange = {
                    onNameChange(it)
                },
                label = {
                    Text("Text")
                }
            )

            CategoryDropDown(
                modifier = Modifier
                    .width(280.dp)
                    .padding(4.dp),
                selectedCategory = if (uiState.categoryId == null) null else uiState.categories.find { it.id == uiState.categoryId },
                categories = uiState.categories,
                onCategorySelected = {

                }
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