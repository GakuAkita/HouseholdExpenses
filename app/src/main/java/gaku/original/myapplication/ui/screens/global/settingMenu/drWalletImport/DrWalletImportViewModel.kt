package gaku.original.myapplication.ui.screens.global.settingMenu.drWalletImport

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.importer.ExpenseImporter
import gaku.original.myapplication.data.importer.ImportRecord
import gaku.original.myapplication.data.importer.ImportedExpenseMapper
import gaku.original.myapplication.data.importer.SkippedRow
import gaku.original.myapplication.data.repository.appTimeZone.AppTimeZoneRepository
import gaku.original.myapplication.data.repository.category.CategoryRepository
import gaku.original.myapplication.data.repository.expense.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID

data class DrWalletImportUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val fileName: String? = null,
    val records: List<ImportRecord> = emptyList(),
    val skipped: List<SkippedRow> = emptyList(),
    /* Category names in the file that the app doesn't have yet. */
    val missingCategoryNames: List<String> = emptyList(),
    val createMissingCategories: Boolean = true,
    val isImporting: Boolean = false,
    val importedCount: Int = 0,
    val isImported: Boolean = false,
) {
    val totalAmount: Long get() = records.sumOf { it.amount }
    val canImport: Boolean get() = records.isNotEmpty() && !isLoading && !isImporting && !isImported
}

class DrWalletImportViewModel(
    private val contentResolver: ContentResolver,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val appTimeZoneRepository: AppTimeZoneRepository,
    private val importer: ExpenseImporter,
    private val mapper: ImportedExpenseMapper,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DrWalletImportUiState())
    val uiState get() = _uiState.asStateFlow()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[APPLICATION_KEY]) as MyApplication
                val container = app.appContainer
                val session = container.sessionContainer!!
                DrWalletImportViewModel(
                    contentResolver = app.contentResolver,
                    expenseRepository = session.expenseRepository,
                    categoryRepository = session.categoryRepository,
                    appTimeZoneRepository = session.appTimeZoneRepository,
                    importer = session.drWalletImporter,
                    mapper = ImportedExpenseMapper(),
                )
            }
        }
    }

    init {
        Timber.d("Created. ${hashCode()}")
    }

    fun onMessageShown() {
        _uiState.update {
            it.copy(
                message = null
            )
        }
    }

    fun onFilePicked(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { DrWalletImportUiState(isLoading = true) }
            try {
                val (fileName, result) = withContext(Dispatchers.IO) {
                    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: throw Exception("Could not open the file.")
                    displayName(uri) to importer.parse(bytes)
                }
                Timber.d("Parsed $fileName: ${result.records.size} records, ${result.skipped.size} skipped")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        fileName = fileName,
                        records = result.records,
                        skipped = result.skipped,
                        missingCategoryNames = mapper.missingCategoryNames(
                            result.records,
                            categoryRepository.categories.value.values
                        ),
                        message = if (result.records.isEmpty()) "No expenses found in the file." else null
                    )
                }
            } catch (e: Exception) {
                Timber.d("Failed to read the file: $e")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = e.message
                    )
                }
            }
        }
    }

    fun onCreateMissingCategoriesChange(create: Boolean) {
        _uiState.update {
            it.copy(
                createMissingCategories = create
            )
        }
    }

    fun onImportClick() {
        val state = _uiState.value
        if (!state.canImport) return
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isImporting = true,
                        importedCount = 0
                    )
                }
                val now = System.currentTimeMillis()
                val categories = categoryRepository.getAllCategories().values.toMutableList()
                if (state.createMissingCategories) {
                    /* Checked again, the categories may have changed since the file was picked. */
                    mapper.missingCategoryNames(state.records, categories).forEach { name ->
                        categories += categoryRepository.addCategory(
                            Category(
                                id = UUID.randomUUID().toString(),
                                timestamp = now,
                                name = name,
                                enabled = true
                            )
                        )
                    }
                }

                val expenses = mapper.toExpenses(
                    records = state.records,
                    source = importer.source,
                    zoneId = appTimeZoneRepository.zoneId.value,
                    categories = categories,
                    timestamp = now
                )
                expenseRepository.addExpenses(expenses) { saved ->
                    _uiState.update { it.copy(importedCount = saved) }
                }
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        isImported = true,
                        message = "Imported ${expenses.size} expenses."
                    )
                }
            } catch (e: Exception) {
                Timber.d("Import failed: $e")
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        message = e.message
                    )
                }
            }
        }
    }

    private fun displayName(uri: Uri): String? =
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }

    override fun onCleared() {
        Timber.d("Cleared. ${hashCode()}")
        super.onCleared()
    }
}
