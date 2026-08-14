package gaku.original.myapplication.ui.screens.global.settingMenu.repeatAdd.editDialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.Frequency
import gaku.original.myapplication.data.dataClass.RepeatAdd
import gaku.original.myapplication.data.repository.repeatAdd.RepeatAddRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

data class RepeatAddEditDialogState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val amount: Long? = null,
    val note: String? = null,
    val itemName: String? = null,
    val storeName: String? = null,
    val category: Category? = null,
    val frequency: Frequency? = null
)

class RepeatAddEditViewModel(
    private val initialRepeatAdd: RepeatAdd? = null,
    private val repeatAddRepository: RepeatAddRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RepeatAddEditDialogState())
    val uiState: StateFlow<RepeatAddEditDialogState> = _uiState.asStateFlow()

    companion object {
        fun Factory(repeatAdd: RepeatAdd?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyApplication
                val container = app.appContainer
                val session = container.sessionContainer!!
                RepeatAddEditViewModel(
                    repeatAdd,
                    session.repeatAddRepository
                )
            }
        }
    }


    init {
        Timber.d("Created. ${hashCode()}")

        if (initialRepeatAdd == null) {
            //新規追加
            _uiState.update {
                it.copy(
                    amount = 0L,
                    note = null,
                    itemName = null,
                    storeName = null,
                    category = null,
                    frequency = null
                )
            }
        } else {
            //編集
            _uiState.update {
                it.copy(
                    amount = initialRepeatAdd.expense.amount,
                    note = initialRepeatAdd.expense.note,
                    itemName = initialRepeatAdd.expense.itemName,
                    storeName = initialRepeatAdd.expense.storeName,
                )
            }
        }
    }

    override fun onCleared() {
        Timber.d("Cleared. ${hashCode()}")
        super.onCleared()
    }
}