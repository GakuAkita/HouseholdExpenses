package gaku.original.myapplication.ui.screens.global.categoryAssignment

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.MainGraph
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.ui.common.CategoryDropDown
import gaku.original.myapplication.ui.common.ConfirmAlertDialog
import gaku.original.myapplication.ui.common.LoadingBox
import gaku.original.myapplication.ui.common.TopBarView
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import timber.log.Timber

@Composable
fun CategoryAssignmentScreenRoot(
    navController: NavController,
    viewModel: CategoryAssignmentViewModel = viewModel(factory = CategoryAssignmentViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = LocalSnackBarHostState.current

    var assignmentToDelete by remember {
        mutableStateOf<CategoryAssignment?>(null)
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(MainGraph.Global.ICategoryAssignment.EditDialog.KEY_UPDATED)
            ?.observe(lifecycleOwner) { updated ->
                if (updated) {
                    Timber.d("Category assignment was updated.")
                    /* load again because value is updated */
                    viewModel.initialize()
                }
            }
    }

    CategoryAssignmentScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackNavClick = {
            navController.popBackStack()
        },
        onAddClick = {
            navController.navigate(MainGraph.Global.ICategoryAssignment.EditDialog(null))
        },
        onEditClick = {
            navController.navigate(MainGraph.Global.ICategoryAssignment.EditDialog(it))
        },
        onCategorySelected = { assignment, categoryId ->
            viewModel.onCategorySelected(assignment, categoryId)
        },
        onDeleteClick = {
            assignmentToDelete = it
        }
    )
    assignmentToDelete?.let { assignment ->
        ConfirmAlertDialog(
            onDismissRequest = {
                assignmentToDelete = null
            },
            onClick = {
                viewModel.onDeleteClick(assignment)
                assignmentToDelete = null
            }
        ) {
            Text("Are you sure to Delete?")
        }
    }
}

@Composable
fun CategoryAssignmentScreen(
    uiState: CategoryAssignmentUiState,
    snackbarHostState: SnackbarHostState,
    onBackNavClick: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (CategoryAssignment) -> Unit,
    onCategorySelected: (AssignmentUiState<CategoryAssignment>, String?) -> Unit,
    onDeleteClick: (CategoryAssignment) -> Unit
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = {
                    onAddClick()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                LoadingBox()
            } else {
                val storeNameAssignment =
                    uiState.assignments.filter {
                        it.assignment is CategoryAssignment.Store
                    } as List<AssignmentUiState<CategoryAssignment.Store>>
                val productNameAssignment =
                    uiState.assignments.filter {
                        it.assignment is CategoryAssignment.Product
                    } as List<AssignmentUiState<CategoryAssignment.Product>>

                if (isLandscape) {
                    /* wide */
                    Row(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        StoreNameAssignmentColumn(
                            modifier = Modifier.weight(1f),
                            assignmentUiList = storeNameAssignment,
                            categories = uiState.categories,
                            onCategorySelected = { assignmentUi, categoryId ->
                                onCategorySelected(assignmentUi, categoryId)
                            },
                            onEditClick = {
                                onEditClick(it)
                            },
                            onDeleteClick = {
                                onDeleteClick(it)
                            }
                        )
                        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        ProductNameAssignmentColumn(
                            modifier = Modifier.weight(1f),
                            assignmentUiList = productNameAssignment,
                            categories = uiState.categories,
                            onCategorySelected = { assignmentUi, categoryId ->
                                onCategorySelected(assignmentUi, categoryId)
                            },
                            onEditClick = {
                                onEditClick(it)
                            },
                            onDeleteClick = {
                                onDeleteClick(it)
                            }
                        )
                    }
                } else {
                    StoreNameAssignmentColumn(
                        modifier = Modifier.weight(1f),
                        assignmentUiList = storeNameAssignment,
                        categories = uiState.categories,
                        onEditClick = {
                            onEditClick(it)
                        },
                        onCategorySelected = { assignmentUi, categoryId ->
                            onCategorySelected(assignmentUi, categoryId)
                        },
                        onDeleteClick = {
                            onDeleteClick(it)
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    ProductNameAssignmentColumn(
                        modifier = Modifier.weight(1f),
                        assignmentUiList = productNameAssignment,
                        categories = uiState.categories,
                        onCategorySelected = { assignmentUi, categoryId ->
                            onCategorySelected(assignmentUi, categoryId)
                        },
                        onEditClick = {
                            onEditClick(it)
                        },
                        onDeleteClick = {
                            onDeleteClick(it)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StoreNameAssignmentColumn(
    modifier: Modifier = Modifier,
    assignmentUiList: List<AssignmentUiState<CategoryAssignment.Store>>,
    categories: List<Category>,
    onEditClick: (CategoryAssignment) -> Unit,
    onDeleteClick: (CategoryAssignment) -> Unit,
    onCategorySelected: (AssignmentUiState<CategoryAssignment>, String?) -> Unit,
) {
    AssignmentListColumn(
        modifier = modifier,
        title = "By store name",
        icon = Icons.Outlined.Storefront,
        assignmentUiList = assignmentUiList,
        categories = categories,
        onEditClick = onEditClick,
        onDeleteClick = onDeleteClick,
        onCategorySelected = onCategorySelected
    )
}

@Composable
fun ProductNameAssignmentColumn(
    modifier: Modifier,
    assignmentUiList: List<AssignmentUiState<CategoryAssignment.Product>>,
    categories: List<Category>,
    onEditClick: (CategoryAssignment) -> Unit,
    onCategorySelected: (AssignmentUiState<CategoryAssignment>, String?) -> Unit,
    onDeleteClick: (CategoryAssignment.Product) -> Unit
) {
    AssignmentListColumn(
        modifier = modifier,
        title = "By product name",
        icon = Icons.Outlined.ShoppingBag,
        assignmentUiList = assignmentUiList,
        categories = categories,
        onEditClick = onEditClick,
        onDeleteClick = { onDeleteClick(it as CategoryAssignment.Product) },
        onCategorySelected = onCategorySelected
    )
}

@Composable
private fun AssignmentListColumn(
    modifier: Modifier,
    title: String,
    icon: ImageVector,
    assignmentUiList: List<AssignmentUiState<CategoryAssignment>>,
    categories: List<Category>,
    onEditClick: (CategoryAssignment) -> Unit,
    onDeleteClick: (CategoryAssignment) -> Unit,
    onCategorySelected: (AssignmentUiState<CategoryAssignment>, String?) -> Unit,
) {
    val lazyListState = rememberLazyListState()

    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
            Text(
                text = "${assignmentUiList.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (assignmentUiList.isEmpty()) {
            Text(
                text = "No rules yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            return@Column
        }

        LazyColumnScrollbar(
            state = lazyListState,
            settings = ScrollbarSettings.Default.copy(
                alwaysShowScrollbar = true,
                thumbUnselectedColor = MaterialTheme.colorScheme.outlineVariant,
                thumbSelectedColor = MaterialTheme.colorScheme.primary
            )
        ) {
            LazyColumn(
                userScrollEnabled = true,
                state = lazyListState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(assignmentUiList.size) { index ->
                    AssignmentItemCard(
                        assignmentUi = assignmentUiList[index],
                        categories = categories,
                        onEditClick = onEditClick,
                        onDeleteClick = onDeleteClick,
                        onCategorySelected = onCategorySelected
                    )
                }
            }
        }
    }
}

@Composable
private fun AssignmentItemCard(
    assignmentUi: AssignmentUiState<CategoryAssignment>,
    categories: List<Category>,
    onEditClick: (CategoryAssignment) -> Unit,
    onDeleteClick: (CategoryAssignment) -> Unit,
    onCategorySelected: (AssignmentUiState<CategoryAssignment>, String?) -> Unit,
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = assignmentUi.assignment.ruleName ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (assignmentUi.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(12.dp)
                            .size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = {
                            onEditClick(assignmentUi.assignment)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = {
                            onDeleteClick(assignmentUi.assignment)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "削除",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            CategoryDropDown(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                selectedCategoryId = assignmentUi.assignment.assignedCategoryId,
                categories = categories,
                onCategoryIdSelected = { categoryId ->
                    onCategorySelected(
                        assignmentUi,
                        categoryId
                    )
                },
                enabled = !assignmentUi.isLoading,
                nullOption = true
            )
        }
    }
}

@Preview
@Composable
fun CategoryAssignmentScreenPreview() {
    val uiState = CategoryAssignmentUiState(
        assignments = listOf(
            AssignmentUiState(
                isLoading = true,
                assignment = CategoryAssignment.Product(
                    id = "1",
                    name = "水",
                    categoryId = "1"
                ),
            ),
            AssignmentUiState(
                isLoading = false,
                assignment = CategoryAssignment.Store(
                    id = "2",
                    name = "はま寿司",
                    categoryId = "2"
                )
            ),
            AssignmentUiState(
                isLoading = true,
                assignment = CategoryAssignment.Store(
                    id = "3",
                    name = "はま寿司2",
                    categoryId = "2"
                )
            ),
            AssignmentUiState(
                isLoading = false,
                assignment = CategoryAssignment.Product(
                    id = "4",
                    name = "アタックaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                    categoryId = "2"
                ),
            ),
            AssignmentUiState(
                isLoading = true,
                assignment = CategoryAssignment.Product(
                    id = "5",
                    name = "アタックaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                    categoryId = "2"
                ),
            )
        )
    )
    CategoryAssignmentScreen(
        uiState = uiState,
        snackbarHostState = SnackbarHostState(),
        onBackNavClick = {},
        onAddClick = {},
        onEditClick = {},
        onCategorySelected = { _, _ -> },
        onDeleteClick = {}
    )
}
private val CategoryAssignment.ruleName: String?
    get() = when (this) {
        is CategoryAssignment.Store -> name
        is CategoryAssignment.Product -> name
    }

private val CategoryAssignment.assignedCategoryId: String?
    get() = when (this) {
        is CategoryAssignment.Store -> categoryId
        is CategoryAssignment.Product -> categoryId
    }
