package gaku.original.myapplication.ui.screens.global.categoryAssignment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.viewModel.settings.CategoryAssignmentEditViewModel

@Composable
fun CategoryAssignmentScreenRoot(
    navController: NavController,
    viewModel: CategoryAssignmentViewModel = viewModel(factory = CategoryAssignmentViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = LocalSnackBarHostState.current

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    CategoryAssignmentScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackNavClick = {
            navController.popBackStack()
        }
    )
}

@Composable
fun CategoryAssignmentScreen(
    uiState: CategoryAssignmentUiState,
    snackbarHostState: SnackbarHostState,
    onBackNavClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopBarView(
                title = "Category Assignment",
                showBackButton = true,
                onBackNavClicked = {
                    onBackNavClick()
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            } else {

            }
        }
    }
}

@Preview
@Composable
fun CategoryAssignmentScreenPreview() {
    val uiState = CategoryAssignmentUiState()
    CategoryAssignmentScreen(
        uiState = uiState,
        snackbarHostState = SnackbarHostState(),
        onBackNavClick = {}
    )
}

@Composable
fun CategoryAssignmentEditView(
    navController: NavController,
    viewModel: CategoryAssignmentEditViewModel = hiltViewModel()
) {
//    val assignmentData = viewModel.assignmentData.collectAsState()
//    val allCategories = viewModel.allCategories.collectAsState()
//    val loading = viewModel.loading.collectAsState()
//
//    var showAddEditDialog by remember { mutableStateOf(false) }
//
//    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
//
//    var assignmentEdited by remember { mutableStateOf<CategoryAssignment?>(null) }
//    var initialNamePattern by remember { mutableStateOf<CategoryAssignNamePattern?>(null) }
//
//    val scope = rememberCoroutineScope()
//    val snackBarHostState = remember { SnackbarHostState() }
//
//    val context = LocalContext.current
//
//    LaunchedEffect(Unit) {
//        viewModel.startInit()
//    }
//
//
//    @Composable
//    fun CategoryAssignmentRow(
//        assignment: CategoryAssignment,
//        namePattern: CategoryAssignNamePattern,
//    ) {
//        Row(
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(0.9f)
//                    .clickable {
//                        assignmentEdited = assignment
//                        initialNamePattern = namePattern
//                        showAddEditDialog = true
//                    },
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .weight(1f)
//                        .padding(horizontal = 5.dp),
//                    text = "${assignment.name}"
//                )
//                CategoryDropDown(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .weight(1f),
//                    initialCategoryId = assignment.categoryId,
//                    categories = allCategories.value,
//                    onCategorySelected = { category ->
//                        viewModel.updateCategoryAssignment(
//                            assignment = assignment.copy(categoryId = category.id),
//                            namePattern = namePattern
//                        ) { result ->
//                            if (result.status != FuncStatus.SUCCESS) {
//                                snackBarHostState.currentSnackbarData?.dismiss()
//                                scope.launch {
//                                    snackBarHostState.showSnackbar(
//                                        "カテゴリー割当の更新に失敗しました: ${result.errorMessage}",
//                                        actionLabel = "OK",
//                                    )
//                                }
//                            }
//                        }
//                    },
//                    nullOption = true
//                )
//            }
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(0.1f),
//                verticalAlignment = Alignment.CenterVertically,
//            ) {
//                IconButton(
//                    onClick = {
//                        assignmentEdited = assignment
//                        initialNamePattern = namePattern
//                        showDeleteConfirmDialog = true//削除のダイアログへ。
//                    }
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Delete,
//                        contentDescription = "削除",
//                    )
//                }
//            }
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopBarView(
//                title = "カテゴリー割当",
//                showBackButton = true,
//                onBackNavClicked = {
//                    navController.popBackStack()
//                }
//            )
//        },
//        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
//        floatingActionButton = {
//            if (snackBarHostState.currentSnackbarData == null) {
//                FloatingActionButtonWithIcon(
//                    onClick = {
//                        /* 増やす */
//                        assignmentEdited = null
//                        initialNamePattern = null
//                        showAddEditDialog = true
//                    },
//                    containerColor = MaterialTheme.colorScheme.tertiary,
//                )
//            }
//        }
//    ) { innerPadding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding)
//        ) {
//            if (loading.value) {
//                CircularProgressIndicator()
//            } else if (assignmentData.value == null) {
//                Text("データ取得に失敗しました。ページを閉じて再度開いてください。")
//            } else {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.Center
//                ) {
//                    Text("いつか検索機能つけます。")
//                }
//                val storeAssignment = assignmentData.value?.storeName
//                val productAssignment = assignmentData.value?.productName
//                Text("店名")
//                Column(
//                    modifier = Modifier
//                        .weight(1f)
//                        .fillMaxWidth()
//                        .padding(2.dp)
//                        .border(width = 1.dp, color = MaterialTheme.colorScheme.tertiary)
//                        .verticalScroll(rememberScrollState())
//                ) {
//                    if (storeAssignment == null) {
//                        Text("店名でのカテゴリー割当がありません")
//                    } else {
//                        val namePattern = CategoryAssignNamePattern.STORE
//                        for ((id, assignment) in storeAssignment.entries.sortedBy { it.value.name }) {
//                            CategoryAssignmentRow(
//                                assignment = assignment,
//                                namePattern
//                            )
//                        }
//                    }
//                }
//                HorizontalDivider()
//                Text("商品名")
//                Column(
//                    modifier = Modifier
//                        .weight(1f)
//                        .fillMaxWidth()
//                        .padding(2.dp)
//                        .border(width = 2.dp, color = MaterialTheme.colorScheme.tertiary)
//                        .verticalScroll(rememberScrollState())
//                ) {
//                    if (productAssignment == null) {
//                        Text("商品名でのカテゴリーの割当がありません")
//                    } else {
//                        val namePattern = CategoryAssignNamePattern.PRODUCT
//                        for ((id, assignment) in productAssignment.entries.sortedBy { it.value.name }) {
//                            CategoryAssignmentRow(
//                                assignment = assignment,
//                                namePattern
//                            )
//                        }
//                    }
//                }
//            }
//
//            if (showAddEditDialog) {
//                CategoryAssignmentDialog(
//                    titleContent = {
//
//                    },
//                    onDismiss = {
//                        showAddEditDialog = false
//                    },
//                    initialAssignment = assignmentEdited,
//                    categories = allCategories.value ?: emptyList(),
//                    onSave = { assignment, namePattern ->
//                        if (assignment.id == null) {
//                            viewModel.addCategoryAssignment(
//                                assignment = assignment,
//                                namePattern = namePattern,
//                            ) {
//                                if (it.status == FuncStatus.SUCCESS) {
//                                    showAddEditDialog = false
//                                    scope.launch {
//                                        snackBarHostState.currentSnackbarData?.dismiss()
//                                        snackBarHostState.showSnackbar(
//                                            "カテゴリー割当を追加しました",
//                                            actionLabel = "OK"
//                                        )
//                                    }
//                                } else {
//                                    Toast.makeText(
//                                        context,
//                                        "カテゴリー割当の追加に失敗しました: ${it.errorMessage}",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                }
//                            }
//                        } else {
//                            viewModel.updateCategoryAssignment(
//                                assignment = assignment,
//                                namePattern = namePattern
//                            ) { result ->
//                                if (result.status == FuncStatus.SUCCESS) {
//                                    showAddEditDialog = false
//                                    scope.launch {
//                                        snackBarHostState.currentSnackbarData?.dismiss()
//                                        snackBarHostState.showSnackbar(
//                                            "カテゴリー割当を更新しました",
//                                            actionLabel = "OK"
//                                        )
//                                    }
//                                } else {
//                                    Toast.makeText(
//                                        context,
//                                        "カテゴリー割当の更新に失敗しました: ${result.errorMessage}",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                }
//                            }
//                        }
//                    },
//                    initialNamePattern = initialNamePattern,
//                    isNamePatternSelectable = initialNamePattern == null
//                )
//            }
//
//            /* 削除してよいかのダイアログ */
//            if (showDeleteConfirmDialog) {
//                ConfirmAlertDialog(
//                    onClick = {
//                        viewModel.removeCategoryAssignment(
//                            assignment = assignmentEdited ?: return@ConfirmAlertDialog,
//                            namePattern = initialNamePattern ?: return@ConfirmAlertDialog
//                        ) { result ->
//                            if (result.status == FuncStatus.SUCCESS) {
//                                showDeleteConfirmDialog = false
//                                scope.launch {
//                                    snackBarHostState.currentSnackbarData?.dismiss()
//                                    snackBarHostState.showSnackbar(
//                                        "カテゴリー割当を削除しました",
//                                        actionLabel = "OK"
//                                    )
//                                }
//                            } else {
//                                Toast.makeText(
//                                    context,
//                                    "カテゴリー割当の削除に失敗しました: ${result.errorMessage}",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                        }
//                    },
//                    onDismissRequest = {
//                        showDeleteConfirmDialog = false
//                    }
//                ) {
//                    Text("${assignmentEdited?.name} を削除しますか？")
//                }
//            }
//        }
//    }
}