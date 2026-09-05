package gaku.original.myapplication.ui.screens.global.categoryAssignment

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.ui.common.CategoryDropDown
import gaku.original.myapplication.ui.common.TopBarView
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings

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
        },
        onCategorySelected = { assignment, categoryId ->
            viewModel.onCategorySelected(assignment, categoryId)
        },
        onDeleteClick = {
            viewModel.onDeleteClick(it)
        }
    )
}

@Composable
fun CategoryAssignmentScreen(
    uiState: CategoryAssignmentUiState,
    snackbarHostState: SnackbarHostState,
    onBackNavClick: () -> Unit,
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
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, MaterialTheme.colorScheme.secondary)
                                .padding(4.dp),
                            assignmentUiList = storeNameAssignment,
                            categories = uiState.categories,
                            onCategorySelected = { assignmentUi, category ->
                                onCategorySelected(assignmentUi, category.id)
                            },
                            onDeleteClick = {
                                onDeleteClick(it)
                            }
                        )
                        ProductNameAssignmentColumn(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, MaterialTheme.colorScheme.secondary)
                                .padding(4.dp),
                            assignmentUiList = productNameAssignment,
                            categories = uiState.categories,
                            onCategorySelected = { assignmentUi, category ->
                                onCategorySelected(assignmentUi, category.id)
                            },
                            onDeleteClick = {
                                onDeleteClick(it)
                            }
                        )
                    }
                } else {
                    StoreNameAssignmentColumn(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, MaterialTheme.colorScheme.secondary)
                            .padding(4.dp),
                        assignmentUiList = storeNameAssignment,
                        categories = uiState.categories,
                        onCategorySelected = { assignmentUi, category ->
                            onCategorySelected(assignmentUi, category.id)
                        },
                        onDeleteClick = {
                            onDeleteClick(it)
                        }
                    )

                    ProductNameAssignmentColumn(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, MaterialTheme.colorScheme.secondary)
                            .padding(4.dp),
                        assignmentUiList = productNameAssignment,
                        categories = uiState.categories,
                        onCategorySelected = { assignmentUi, category ->
                            onCategorySelected(assignmentUi, category.id)
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
    onDeleteClick: (CategoryAssignment) -> Unit,
    onCategorySelected: (AssignmentUiState<CategoryAssignment>, Category) -> Unit,
) {
    val lazyListState = rememberLazyListState()

    Column(
        modifier = modifier
    ) {
        Text("Category Assignment by Store name")
        LazyColumnScrollbar(
            state = lazyListState,
            settings = ScrollbarSettings.Default.copy(
                alwaysShowScrollbar = true,
                thumbUnselectedColor = MaterialTheme.colorScheme.secondary,
                thumbSelectedColor = MaterialTheme.colorScheme.primary
            )
        ) {
            LazyColumn(
                userScrollEnabled = true,
                state = lazyListState,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(assignmentUiList.size) { index ->
                    val assignmentUi = assignmentUiList[index]
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
                        Text("${assignmentUi.assignment.name}")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            CategoryDropDown(
                                modifier = Modifier.widthIn(max = 220.dp),
                                selectedCategoryId = assignmentUi.assignment.categoryId,
                                categories = categories,
                                onCategorySelected = { category ->
                                    onCategorySelected(
                                        assignmentUi,
                                        category
                                    )
                                },
                                enabled = !assignmentUi.isLoading,
                                nullOption = true
                            )
                            Column(
                                modifier = Modifier.width(54.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (assignmentUi.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(40.dp)
                                    )
                                } else {
                                    IconButton(
                                        onClick = {
                                            onDeleteClick(assignmentUi.assignment)
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
        }
    }
}

@Composable
fun ProductNameAssignmentColumn(
    modifier: Modifier,
    assignmentUiList: List<AssignmentUiState<CategoryAssignment.Product>>,
    categories: List<Category>,
    onCategorySelected: (AssignmentUiState<CategoryAssignment>, Category) -> Unit,
    onDeleteClick: (CategoryAssignment.Product) -> Unit
) {
    val lazyListState = rememberLazyListState()
    Column(
        modifier = modifier
    ) {
        Text("Category Assignment by Product name")
        LazyColumnScrollbar(
            state = lazyListState,
            settings = ScrollbarSettings.Default.copy(
                alwaysShowScrollbar = true,
                thumbUnselectedColor = MaterialTheme.colorScheme.secondary,
                thumbSelectedColor = MaterialTheme.colorScheme.primary
            ),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                state = lazyListState,
                userScrollEnabled = true
            ) {
                items(assignmentUiList.size) { index ->
                    val productUi = assignmentUiList[index]
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
                        Text("${productUi.assignment.name}")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            CategoryDropDown(
                                modifier = Modifier.widthIn(max = 220.dp),
                                selectedCategoryId = productUi.assignment.categoryId,
                                categories = categories,
                                onCategorySelected = { categoryId ->
                                    onCategorySelected(productUi, categoryId)
                                },
                                nullOption = true,
                                enabled = !productUi.isLoading
                            )
                            Column(
                                modifier = Modifier.width(54.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (productUi.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(40.dp)
                                    )
                                } else {
                                    IconButton(
                                        onClick = {
                                            onDeleteClick(productUi.assignment)
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
        }
    }
}

@Preview
@Composable
fun CategoryAssignmentScreenPreview() {
    val uiState = CategoryAssignmentUiState(
        assignments = listOf(
            AssignmentUiState(
                isLoading = false,
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
        onCategorySelected = { _, _ -> },
        onDeleteClick = {}
    )
}