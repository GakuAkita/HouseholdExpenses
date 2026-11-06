package gaku.original.myapplication.ui.screens.global.rakutenCardExclude

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.dataClass.MatchCondition
import gaku.original.myapplication.data.dataClass.RakutenCardExcludeItem
import gaku.original.myapplication.data.repository.rakutenCardExclude.RakutenCardExcludeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class RakutenCardAllExcludeUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val items: List<ExcludeItemUiState> = emptyList(),

    val isAddDialogShown: Boolean = false,
    val isSaving: Boolean = false,
    val name: String = "",
    val condition: MatchCondition = MatchCondition.EXACT
)

data class ExcludeItemUiState(
    val isLoading: Boolean = false,
    val item: RakutenCardExcludeItem
)

class RakutenCardAllExcludeViewModel(
    private val rakutenCardExcludeRepository: RakutenCardExcludeRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RakutenCardAllExcludeUiState())
    val uiState = _uiState.asStateFlow()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyApplication
                val session = app.appContainer.sessionContainer!!
                RakutenCardAllExcludeViewModel(session.rakutenCardExcludeRepository)
            }
        }
    }

    init {
        Timber.d("${hashCode()} Created.")
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val items = rakutenCardExcludeRepository.getExcludeItems()
                _uiState.update {
                    it.copy(
                        items = items.values.map { item -> ExcludeItemUiState(item = item) },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load the exclude items.")
                _uiState.update { it.copy(message = e.message, isLoading = false) }
            }
        }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }

    fun onAddClick() {
        _uiState.update {
            it.copy(
                isAddDialogShown = true,
                name = "",
                condition = MatchCondition.EXACT
            )
        }
    }

    fun onAddDialogDismiss() {
        _uiState.update {
            /* The item is being saved, so closing now would hide a failure. */
            if (it.isSaving) it else it.copy(isAddDialogShown = false)
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onConditionSelected(condition: MatchCondition) {
        _uiState.update { it.copy(condition = condition) }
    }

    fun onSaveClick() {
        val state = _uiState.value
        if (state.isSaving) {
            return
        }
        /* Stores are compared with the name in the mail, where a stray space would never match. */
        val name = state.name.trim()
        if (name.isEmpty()) {
            _uiState.update { it.copy(message = "Store name is empty.") }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true) }
                rakutenCardExcludeRepository.addExcludeItem(
                    RakutenCardExcludeItem(name = name, condition = state.condition)
                )
                _uiState.update { it.copy(isSaving = false, isAddDialogShown = false) }
                loadItems()
            } catch (e: Exception) {
                Timber.e(e, "Failed to add the exclude item.")
                _uiState.update { it.copy(message = e.message, isSaving = false) }
            }
        }
    }

    fun onDeleteClick(item: RakutenCardExcludeItem) {
        viewModelScope.launch {
            try {
                setItemLoading(item, true)
                rakutenCardExcludeRepository.deleteExcludeItem(item)
                _uiState.update {
                    it.copy(items = it.items.filter { itemUi -> itemUi.item.id != item.id })
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete the exclude item.")
                setItemLoading(item, false)
                _uiState.update { it.copy(message = e.message) }
            }
        }
    }

    private fun setItemLoading(item: RakutenCardExcludeItem, isLoading: Boolean) {
        _uiState.update {
            it.copy(
                items = it.items.map { itemUi ->
                    if (itemUi.item.id == item.id) itemUi.copy(isLoading = isLoading) else itemUi
                }
            )
        }
    }

    override fun onCleared() {
        Timber.d("${hashCode()} Cleared")
        super.onCleared()
    }
}
