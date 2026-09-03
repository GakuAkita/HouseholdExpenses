package gaku.original.myapplication.ui.screens.global.categoryAssignment

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.ui.common.CategoryDropDown
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
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    Scaffold(
        topBar = {
            TopBarView(
                title = "Category Assignment",
                showBackButton = true,
                onBackNavClicked = {
                    onBackNavClick()
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
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
                val storeNameAssignment =
                    uiState.assignments.filterIsInstance<CategoryAssignment.Store>()
                val productNameAssignment =
                    uiState.assignments.filterIsInstance<CategoryAssignment.Product>()

                if (isLandscape) {
                    /* wide */
                    Row(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        StoreNameAssignmentColumn(
                            modifier = Modifier.weight(1f),
                            assignments = storeNameAssignment,
                            categories = uiState.categories,
                            onCategorySelected = { index, category -> },
                            onDeleteClick = {}
                        )
                        ProductNameAssignmentColumn(
                            modifier = Modifier.weight(1f),
                            assignments = productNameAssignment,
                            categories = uiState.categories,
                            onCategorySelected = { index, category ->
                            },
                            onDeleteClick = {}
                        )
                    }
                } else {
                    StoreNameAssignmentColumn(
                        modifier = Modifier.weight(1f),
                        assignments = storeNameAssignment,
                        categories = uiState.categories,
                        onCategorySelected = { index, category ->

                        },
                        onDeleteClick = {}
                    )

                    ProductNameAssignmentColumn(
                        modifier = Modifier.weight(1f),
                        assignments = productNameAssignment,
                        categories = uiState.categories,
                        onCategorySelected = { index, category ->

                        },
                        onDeleteClick = {}
                    )
                }
            }
        }
    }
}

@Composable
fun StoreNameAssignmentColumn(
    modifier: Modifier = Modifier,
    assignments: List<CategoryAssignment.Store>,
    categories: List<Category>,
    onDeleteClick: (CategoryAssignment.Store) -> Unit,
    onCategorySelected: (Int, Category) -> Unit,
) {
    Column(
        modifier = modifier
    ) {
        Text("Category Assignment by Store name")
        Column(
            modifier = modifier.verticalScroll(rememberScrollState()),
        ) {
            assignments.forEachIndexed { index, store ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            vertical = 4.dp,
                            horizontal = 4.dp
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                ) {
                    Text("${store.name}")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        CategoryDropDown(
                            modifier = Modifier.widthIn(max = 220.dp),
                            initialCategoryId = store.categoryId,
                            categories = categories,
                            onCategorySelected = { category ->
                                onCategorySelected(
                                    index,
                                    category
                                )
                            },
                            nullOption = true
                        )
                        IconButton(
                            onClick = {
                                onDeleteClick(store)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "削除"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductNameAssignmentColumn(
    modifier: Modifier,
    assignments: List<CategoryAssignment.Product>,
    categories: List<Category>,
    onCategorySelected: (Int, Category) -> Unit,
    onDeleteClick: (CategoryAssignment.Product) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Text("Category Assignment by Product name")
        Column(
            modifier = modifier.verticalScroll(rememberScrollState())
        ) {
            assignments.forEachIndexed { index, assignment ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 4.dp,
                            vertical = 4.dp
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                ) {
                    Text("${assignment.name}")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        CategoryDropDown(
                            modifier = Modifier.widthIn(max = 220.dp),
                            initialCategoryId = assignment.categoryId,
                            categories = categories,
                            onCategorySelected = { categoryId ->
                                onCategorySelected(index, categoryId)
                            },
                            nullOption = true
                        )
                        IconButton(
                            onClick = {
                                onDeleteClick(assignment)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "削除"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun CategoryAssignmentScreenPreview() {
    val uiState = CategoryAssignmentUiState(
        assignments = listOf(
            CategoryAssignment.Product(
                id = "1",
                name = "水",
                categoryId = "1"
            ),
            CategoryAssignment.Store(
                id = "2",
                name = "はま寿司",
                categoryId = "2"
            ),
            CategoryAssignment.Store(
                id = "3",
                name = "はま寿司2",
                categoryId = "2"
            ),
            CategoryAssignment.Product(
                id = "4",
                name = "アタックaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                categoryId = "2"
            ),
            CategoryAssignment.Product(
                id = "5",
                name = "アタックaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                categoryId = "2"
            ),
        )
    )
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