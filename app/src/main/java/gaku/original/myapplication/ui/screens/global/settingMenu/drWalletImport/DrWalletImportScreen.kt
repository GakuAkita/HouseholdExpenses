package gaku.original.myapplication.ui.screens.global.settingMenu.drWalletImport

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.data.importer.DrWalletRecord
import gaku.original.myapplication.data.importer.SkippedRow
import gaku.original.myapplication.ui.common.TopBarView
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/* Some file providers label CSV as Excel or plain text. */
private val CSV_MIME_TYPES = arrayOf(
    "text/csv",
    "text/comma-separated-values",
    "text/plain",
    "application/csv",
    "application/vnd.ms-excel",
)

private val rowDateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")

private val yenFormat = NumberFormat.getCurrencyInstance(Locale.JAPAN)

@Composable
fun DrWalletImportScreenRoot(
    viewModel: DrWalletImportViewModel = viewModel(factory = DrWalletImportViewModel.Factory),
    navHostController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackBarHostState = LocalSnackBarHostState.current
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackBarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.onFilePicked(it) }
    }

    DrWalletImportScreen(
        uiState,
        snackBarHostState,
        onBackNavClick = {
            navHostController.popBackStack()
        },
        onChooseFileClick = {
            filePicker.launch(CSV_MIME_TYPES)
        },
        onCreateMissingCategoriesChange = {
            viewModel.onCreateMissingCategoriesChange(it)
        },
        onImportClick = {
            viewModel.onImportClick()
        }
    )
}

@Composable
fun DrWalletImportScreen(
    uiState: DrWalletImportUiState,
    snackbarHostState: SnackbarHostState,
    onBackNavClick: () -> Unit,
    onChooseFileClick: () -> Unit,
    onCreateMissingCategoriesChange: (Boolean) -> Unit,
    onImportClick: () -> Unit,
) {
    var isShowSkipped by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopBarView(
                title = "Import from Dr.Wallet",
                onBackNavClicked = { onBackNavClick() },
                showBackButton = true,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(Modifier.padding(4.dp))
                Text(
                    "Choose a CSV file exported from Dr.Wallet. " +
                            "Importing the same file again does not add duplicates.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            item {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading && !uiState.isImporting,
                    onClick = onChooseFileClick
                ) {
                    Text(if (uiState.fileName == null) "Choose CSV file" else "Choose another file")
                }
            }

            if (uiState.isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (uiState.records.isNotEmpty() || uiState.skipped.isNotEmpty()) {
                item {
                    ImportSummary(uiState)
                }
            }

            if (uiState.skipped.isNotEmpty()) {
                item {
                    TextButton(onClick = { isShowSkipped = !isShowSkipped }) {
                        Text(
                            "${if (isShowSkipped) "Hide" else "Show"} ${uiState.skipped.size} skipped rows"
                        )
                    }
                }
                if (isShowSkipped) {
                    items(uiState.skipped, key = { "skipped_${it.rowNumber}" }) {
                        SkippedRowView(it)
                    }
                }
            }

            if (uiState.missingCategoryNames.isNotEmpty() && !uiState.isImported) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Create ${uiState.missingCategoryNames.size} new categories")
                            Text(
                                uiState.missingCategoryNames.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (!uiState.createMissingCategories) {
                                Text(
                                    "Expenses in these categories will be uncategorized.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        Switch(
                            checked = uiState.createMissingCategories,
                            enabled = !uiState.isImporting,
                            onCheckedChange = onCreateMissingCategoriesChange
                        )
                    }
                }
            }

            if (uiState.records.isNotEmpty()) {
                item {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.canImport,
                        onClick = onImportClick
                    ) {
                        Text(
                            when {
                                uiState.isImported -> "Imported ${uiState.importedCount} expenses"
                                uiState.isImporting -> "Importing ${uiState.importedCount} / ${uiState.records.size}"
                                else -> "Import ${uiState.records.size} expenses"
                            }
                        )
                    }
                }

                item {
                    HorizontalDivider()
                }

                items(uiState.records, key = { "record_${it.rowNumber}" }) {
                    RecordRow(it)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ImportSummary(uiState: DrWalletImportUiState) {
    Column {
        uiState.fileName?.let {
            Text(it, style = MaterialTheme.typography.titleMedium)
        }
        if (uiState.records.isNotEmpty()) {
            val first = uiState.records.minOf { it.datetime }.toLocalDate()
            val last = uiState.records.maxOf { it.datetime }.toLocalDate()
            Text("${uiState.records.size} expenses, ${yenFormat.format(uiState.totalAmount)}")
            Text("$first – $last", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun RecordRow(record: DrWalletRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                record.datetime.format(rowDateFormatter),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                listOfNotNull(record.storeName, record.itemName).joinToString(" / ").ifEmpty { "-" },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            record.categoryName?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(yenFormat.format(record.amount))
    }
}

@Composable
private fun SkippedRowView(row: SkippedRow) {
    Text(
        "Row ${row.rowNumber}: ${row.reason}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview
@Composable
fun DrWalletImportScreenPreview() {
    val uiState = DrWalletImportUiState(
        fileName = "drwallet_2026.csv",
        records = listOf(
            DrWalletRecord(2, LocalDateTime.of(2026, 9, 15, 12, 0), 1200, "食費", "スーパー", "野菜", null),
            DrWalletRecord(3, LocalDateTime.of(2026, 9, 16, 12, 0), 150, "カフェ", "コーヒー店", null, null),
        ),
        skipped = listOf(SkippedRow(4, "Income")),
        missingCategoryNames = listOf("カフェ"),
    )

    DrWalletImportScreen(
        uiState,
        snackbarHostState = SnackbarHostState(),
        onBackNavClick = {},
        onChooseFileClick = {},
        onCreateMissingCategoriesChange = {},
        onImportClick = {}
    )
}
