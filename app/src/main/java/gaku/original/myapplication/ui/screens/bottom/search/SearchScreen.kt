package gaku.original.myapplication.ui.screens.bottom.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.MainGraph
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.ExpenseSearchFilter
import gaku.original.myapplication.data.dataClass.GeneratedTypeKind
import gaku.original.myapplication.ui.common.EmptyState
import gaku.original.myapplication.ui.common.toYen
import gaku.original.myapplication.ui.screens.global.expenseAddEdit.ExpenseAddEditMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun SearchScreenRoot(
    viewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory),
    rootNavController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackBarHostState.current

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    SearchScreen(
        uiState = uiState,
        onKeywordChanged = viewModel::onKeywordChanged,
        onCategoryToggled = viewModel::onCategoryToggled,
        onGeneratedTypeToggled = viewModel::onGeneratedTypeToggled,
        onPeriodSelected = viewModel::onPeriodSelected,
        onClearFilter = viewModel::onClearFilter,
        onRefresh = viewModel::onRefresh,
        onResultClick = { id ->
            viewModel.getExpense(id)?.let {
                rootNavController.navigate(
                    MainGraph.Global.ExpenseAddEdit(ExpenseAddEditMode.Edit(it))
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    uiState: SearchUiState,
    onKeywordChanged: (String) -> Unit,
    onCategoryToggled: (String?) -> Unit,
    onGeneratedTypeToggled: (GeneratedTypeKind) -> Unit,
    onPeriodSelected: (SearchPeriod) -> Unit,
    onClearFilter: () -> Unit,
    onRefresh: () -> Unit,
    onResultClick: (String) -> Unit,
) {
    /* Results are not updated in real time. Pull down to read them again. */
    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        /* Filters scroll away with the results so that the list gets the whole screen. */
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                SearchFilterPanel(
                    uiState = uiState,
                    onKeywordChanged = onKeywordChanged,
                    onCategoryToggled = onCategoryToggled,
                    onGeneratedTypeToggled = onGeneratedTypeToggled,
                    onPeriodSelected = onPeriodSelected,
                    onClearFilter = onClearFilter,
                )
            }

            item {
                SearchSummary(uiState = uiState)
            }

            items(uiState.results, key = { it.id }) { result ->
                SearchResultItem(
                    result = result,
                    onClick = { onResultClick(result.id) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )
            }

            if (!uiState.isLoading && uiState.results.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.SearchOff,
                        title = "No expenses match the conditions",
                        message = "Try a different keyword or clear some filters."
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchFilterPanel(
    uiState: SearchUiState,
    onKeywordChanged: (String) -> Unit,
    onCategoryToggled: (String?) -> Unit,
    onGeneratedTypeToggled: (GeneratedTypeKind) -> Unit,
    onPeriodSelected: (SearchPeriod) -> Unit,
    onClearFilter: () -> Unit,
) {
    val filter = uiState.filter

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = filter.keyword,
            onValueChange = onKeywordChanged,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = MaterialTheme.shapes.extraLarge,
            placeholder = { Text("Store, item or note") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (filter.keyword.isNotEmpty()) {
                    IconButton(onClick = { onKeywordChanged("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear keyword")
                    }
                }
            }
        )

        FilterSection(title = "Category") {
            FilterChip(
                selected = null in filter.categoryIds,
                onClick = { onCategoryToggled(null) },
                label = { Text("Uncategorized") }
            )
            /* Disabled categories are hidden unless they are already selected. */
            uiState.categories
                .filter { it.enabled || it.id in filter.categoryIds }
                .forEach { category ->
                    FilterChip(
                        selected = category.id in filter.categoryIds,
                        onClick = { onCategoryToggled(category.id) },
                        label = { Text(category.name) }
                    )
                }
        }

        FilterSection(title = "Created by") {
            GeneratedTypeKind.entries.forEach { kind ->
                FilterChip(
                    selected = kind in filter.generatedTypes,
                    onClick = { onGeneratedTypeToggled(kind) },
                    label = { Text(kind.label()) }
                )
            }
        }

        FilterSection(title = "Period") {
            SearchPeriod.entries.forEach { period ->
                FilterChip(
                    selected = period == uiState.period,
                    onClick = { onPeriodSelected(period) },
                    label = { Text(period.label) }
                )
            }
        }

        if (!filter.isEmpty()) {
            TextButton(
                onClick = onClearFilter,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Clear conditions")
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    chips: @Composable () -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 12.dp, bottom = 2.dp)
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chips()
    }
}

@Composable
private fun SearchSummary(uiState: SearchUiState) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (uiState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.results.size} expenses",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = uiState.total.toYen(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

private val resultDateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")

@Composable
private fun SearchResultItem(
    result: SearchResultUi,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = result.datetime.format(resultDateFormatter),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = result.amount.toYen(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = result.categoryName ?: "Uncategorized",
                style = MaterialTheme.typography.bodyMedium,
                color = if (result.categoryName == null) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = result.generatedType.label(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        MaterialTheme.shapes.extraSmall
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        val description = listOfNotNull(result.storeName, result.itemName, result.note)
            .filter { it.isNotBlank() }
            .joinToString(" / ")
        if (description.isNotEmpty()) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun GeneratedTypeKind.label(): String = when (this) {
    GeneratedTypeKind.Manual -> "Manual"
    GeneratedTypeKind.RepeatAdd -> "Repeat add"
    GeneratedTypeKind.MailExtraction -> "Mail extraction"
    GeneratedTypeKind.Import -> "Import"
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    val food = Category(id = "food", timestamp = 0L, name = "食費", enabled = true)
    val uiState = SearchUiState(
        filter = ExpenseSearchFilter(
            categoryIds = setOf(null),
            generatedTypes = setOf(GeneratedTypeKind.MailExtraction)
        ),
        categories = listOf(
            food,
            Category(id = "daily", timestamp = 1L, name = "日用品", enabled = true)
        ),
        results = listOf(
            SearchResultUi(
                id = "1",
                datetime = LocalDateTime.now(),
                amount = 1200,
                categoryName = null,
                storeName = "Amazon",
                itemName = "USB cable",
                note = null,
                generatedType = GeneratedTypeKind.MailExtraction
            ),
            SearchResultUi(
                id = "2",
                datetime = LocalDateTime.now().minusDays(3),
                amount = 3400,
                categoryName = null,
                storeName = "楽天ペイ",
                itemName = null,
                note = null,
                generatedType = GeneratedTypeKind.MailExtraction
            ),
        ),
        total = 4600
    )
    SearchScreen(
        uiState = uiState,
        onKeywordChanged = {},
        onCategoryToggled = {},
        onGeneratedTypeToggled = {},
        onPeriodSelected = {},
        onClearFilter = {},
        onRefresh = {},
        onResultClick = {}
    )
}
