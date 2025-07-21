package gaku.original.myapplication.ui.view.settings.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.ui.common.CategoryAssignmentDialog
import gaku.original.myapplication.ui.common.FloatingActionButtonWithIcon
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.viewModel.settings.CategoryAssignmentEditViewModel

@Composable
fun CategoryAssignmentEditView(
    navController: NavController,
    viewModel: CategoryAssignmentEditViewModel = hiltViewModel()
) {
    val assignmentData = viewModel.assignmentData.collectAsState()
    val allCategories = viewModel.allCategories.collectAsState()
    val loading = viewModel.loading.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.startInit()
    }

    Scaffold(
        topBar = {
            TopBarView(
                title = "カテゴリー割当",
                showBackButton = true,
                onBackNavClicked = {
                    navController.popBackStack()
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        floatingActionButton = {
            FloatingActionButtonWithIcon(
                onClick = {
                    /* 増やす */
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.tertiary,
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (loading.value) {
                CircularProgressIndicator()
            } else if (assignmentData.value == null) {
                Text("データ取得に失敗しました。ページを閉じて再度開いてください。")
            } else {
                val storeAssignment = assignmentData.value?.storeName
                val productAssignment = assignmentData.value?.productName

                if (storeAssignment == null) {
                    Text("ストアの割当がありません")
                } else {
                    for ((id, assignment) in storeAssignment) {
                        CategoryAssignmentRow(
                            assignment = assignment
                        )
                    }
                }

                if (productAssignment == null) {
                    Text("商品カテゴリーの割当がありません")
                } else {

                }
            }

            if (showAddEditDialog) {
                CategoryAssignmentDialog(
                    titleContent = {

                    },
                    onDismiss = {
                        showAddEditDialog = false
                    },
                    initialAssignment = null,
                    categories = allCategories.value ?: emptyList(),
                    onSave = { assignment, namePattern ->
                    },
                    isNamePatternSelectable = true
                )
            }
        }
    }
}

@Composable
fun CategoryAssignmentRow(
    assignment: CategoryAssignment,
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 5.dp),
            text = "${assignment.name}"
        )
    }
}