package gaku.original.myapplication.ui.view.settings.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.dataClass.AssignmentCondition
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.viewModel.settings.CategoryAssignmentEditViewModel
import kotlinx.coroutines.launch

@Composable
fun CategoryAssignmentEditView(
    navController: NavController,
    viewModel: CategoryAssignmentEditViewModel = hiltViewModel()
) {
    val assignmentData = viewModel.assignmentData.collectAsState()
    val allCategories = viewModel.allCategories.collectAsState()
    val loading = viewModel.loading.collectAsState()

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
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (loading.value) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        viewModel.addCategoryAssignment(
                            CategoryAssignment(
                                name = "テストカテゴリー",
                                categoryId = allCategories.value.getOrNull(0)?.id,
                                condition = AssignmentCondition.EXACT_MATCH
                            ),
                            callback = {
                                if (it.status != SuspendFuncStatus.SUCCESS) {
                                    scope.launch {
                                        snackBarHostState.showSnackbar(
                                            message = "カテゴリー割当の追加に失敗しました: ${it.errorMessage}"
                                        )
                                    }
                                }
                            }
                        )
                    }
                ) {
                    Text("テスト")
                }
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