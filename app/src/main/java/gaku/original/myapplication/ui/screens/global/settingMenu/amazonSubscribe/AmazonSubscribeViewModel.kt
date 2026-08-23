package gaku.original.myapplication.ui.screens.global.settingMenu.amazonSubscribe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.dataClass.AmazonSubscribeItem
import gaku.original.myapplication.data.repository.amazonSubscribeItem.AmazonSubscribeItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class AmazonSubscribeUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val amazonSubscribeItems: List<AmazonSubscribeItemUiState> = emptyList(),
    val isShowDisabledItems: Boolean = false,
    val isLoadError: Boolean = false
)

data class AmazonSubscribeItemUiState(
    val subscribeItem: AmazonSubscribeItem,
    val isLoading: Boolean = false
)

fun AmazonSubscribeUiState.updateItem(newState: AmazonSubscribeItemUiState): AmazonSubscribeUiState {
    for (item in amazonSubscribeItems) {
        if (item.subscribeItem.id == newState.subscribeItem.id) {
            return this.copy(
                amazonSubscribeItems = amazonSubscribeItems.map {
                    if (it.subscribeItem.id == newState.subscribeItem.id) {
                        newState
                    } else {
                        it
                    }
                }
            )
        }
    }
    throw Exception("Unable to find subscribe item to update.")
}

class AmazonSubscribeViewModel(
    private val amazonSubscribeItemRepository: AmazonSubscribeItemRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AmazonSubscribeUiState())
    val uiState: StateFlow<AmazonSubscribeUiState> get() = _uiState

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyApplication
                val container = app.appContainer
                val session = container.sessionContainer!!
                AmazonSubscribeViewModel(
                    amazonSubscribeItemRepository = session.amazonSubscribeItemRepository
                )
            }
        }
    }

    init {
        Timber.d("Created. ${hashCode()}")
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isLoading = true
                    )
                }
                val mapData = amazonSubscribeItemRepository.getAllAmazonSubscribeItems()
                _uiState.update {
                    it.copy(
                        amazonSubscribeItems = mapData.values.map { it ->
                            AmazonSubscribeItemUiState(
                                subscribeItem = it,
                                isLoading = false
                            )
                        },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = e.message,
                        isLoadError = true
                    )
                }

            }
        }
    }

    fun onMessageShown() {
        _uiState.update {
            it.copy(
                message = null
            )
        }
    }

    fun onShowDisabledItemsClick() {
        _uiState.update {
            it.copy(
                isShowDisabledItems = !it.isShowDisabledItems
            )
        }
    }

    private suspend fun EnableItem(itemUiState: AmazonSubscribeItemUiState, enabled: Boolean) {
        try {
            val loadingState = itemUiState.copy(
                isLoading = true
            )
            _uiState.update {
                it.updateItem(loadingState)
            }

            val newItem = itemUiState.subscribeItem.copy(
                enabled = enabled
            )

            amazonSubscribeItemRepository.updateAmazonSubscribeItem(newItem)

            /* Loading has ended and state was updated */
            _uiState.update {
                it.updateItem(
                    itemUiState.copy(
                        subscribeItem = newItem,
                        isLoading = false
                    )
                )
            }
        } catch (e: Exception) {
            /* Failed */
            _uiState.update {
                it.updateItem(
                    itemUiState.copy(
                        isLoading = false
                    )
                ).copy(
                    message = e.message
                )
            }
        }
    }

    /* This is technically to disable. */
    fun onDeleteClick(itemUiState: AmazonSubscribeItemUiState) {
        viewModelScope.launch {
            EnableItem(itemUiState, false)
        }
    }

    fun onRestoreClick(itemUiState: AmazonSubscribeItemUiState) {
        viewModelScope.launch {
            EnableItem(itemUiState, true)
        }
    }

    override fun onCleared() {
        Timber.d("Cleared. ${hashCode()}")
        super.onCleared()
    }
}

//@HiltViewModel
//class AmazonSubscribeItemsViewModel @Inject constructor(
//    private val amazonSubscribeItemsRepository: AmazonSubscribeItemsRTDbRepository
//) : ViewModel() {
//
//    private val className: String = this::class.java.simpleName
//
//    // UI状態管理
//    private val _loadingStatus = MutableStateFlow(LoadingStatus.IDLE)
//    val loadingStatus: StateFlow<LoadingStatus> = _loadingStatus
//
//    private val _amazonSubscribeItems =
//        MutableStateFlow<Map<String, AmazonSubscribeItem>>(emptyMap())
//    val amazonSubscribeItems: StateFlow<Map<String, AmazonSubscribeItem>> = _amazonSubscribeItems
//
//    private val _disabledAmazonSubscribeItems =
//        MutableStateFlow<Map<String, AmazonSubscribeItem>>(emptyMap())
//    val disabledAmazonSubscribeItems: StateFlow<Map<String, AmazonSubscribeItem>> =
//        _disabledAmazonSubscribeItems
//
//    private val _errorMessage = MutableStateFlow<String?>(null)
//    val errorMessage: StateFlow<String?> = _errorMessage
//
//    init {
//        // 画面起動時にデータをロード
//        loadAmazonSubscribeItems()
//    }
//
//    /**
//     * Amazon定期便アイテムをロード
//     */
//    fun loadAmazonSubscribeItems() {
//        viewModelScope.launch {
//            try {
//                _loadingStatus.value = LoadingStatus.LOADING
//                _errorMessage.value = null
//
//                val result = amazonSubscribeItemsRepository.getAllAmazonSubscribeItems()
//
//                when (result) {
//                    is FuncResultWithData.Success -> {
//                        val allItems = result.data ?: emptyMap()
//                        // enabledがtrueのアイテムのみをフィルタリング
//                        val enabledItems = allItems
//                            .filter { (_, item) -> item.enabled != false }
//                        // enabledがfalseのアイテムをフィルタリング
//                        val disabledItems = allItems
//                            .filter { (_, item) -> item.enabled == false }
//                        _amazonSubscribeItems.value = enabledItems
//                        _disabledAmazonSubscribeItems.value = disabledItems
//                        _loadingStatus.value = LoadingStatus.SUCCESS
//                        Log.d(
//                            className,
//                            "Successfully loaded ${enabledItems.size} enabled and ${disabledItems.size} disabled Amazon Subscribe items"
//                        )
//                    }
//
//                    is FuncResultWithData.Failure -> {
//                        _errorMessage.value = result.errorMessage
//                        _loadingStatus.value = LoadingStatus.ERROR
//                        Log.e(
//                            className,
//                            "Failed to load Amazon Subscribe items: ${result.errorMessage}"
//                        )
//                    }
//
//                    is FuncResultWithData.Warning -> {
//                        _errorMessage.value = result.warningMessage
//                        _loadingStatus.value = LoadingStatus.ERROR
//                        Log.w(
//                            className,
//                            "Warning while loading Amazon Subscribe items: ${result.warningMessage}"
//                        )
//                    }
//                }
//            } catch (e: Exception) {
//                _errorMessage.value = "Unexpected error: ${e.message}"
//                _loadingStatus.value = LoadingStatus.ERROR
//                Log.e(className, "Exception while loading Amazon Subscribe items", e)
//            }
//        }
//    }
//
//    /**
//     * 手動リフレッシュ
//     */
//    fun refresh() {
//        loadAmazonSubscribeItems()
//    }
//
//    /**
//     * アイテムを無効化する（enabledをfalseに設定）
//     */
//    fun disableItem(item: AmazonSubscribeItem) {
//        viewModelScope.launch {
//            try {
//                val result = amazonSubscribeItemsRepository.disableAmazonSubscribeItem(item)
//
//                when (result.status) {
//                    FuncStatus.SUCCESS -> {
//                        // 成功時はリストを再読み込み
//                        loadAmazonSubscribeItems()
//                        Log.d(className, "Successfully disabled Amazon Subscribe item: ${item.id}")
//                    }
//
//                    else -> {
//                        _errorMessage.value = result.errorMessage ?: "Failed to disable item"
//                        Log.e(
//                            className,
//                            "Failed to disable Amazon Subscribe item: ${result.errorMessage}"
//                        )
//                    }
//                }
//            } catch (e: Exception) {
//                _errorMessage.value = "Unexpected error: ${e.message}"
//                Log.e(className, "Exception while disabling Amazon Subscribe item", e)
//            }
//        }
//    }
//
//    /**
//     * アイテムを有効化する（enabledをtrueに設定）
//     */
//    fun enableItem(item: AmazonSubscribeItem) {
//        viewModelScope.launch {
//            try {
//                val result = amazonSubscribeItemsRepository.enableAmazonSubscribeItem(item)
//
//                when (result.status) {
//                    FuncStatus.SUCCESS -> {
//                        // 成功時はリストを再読み込み
//                        loadAmazonSubscribeItems()
//                        Log.d(className, "Successfully enabled Amazon Subscribe item: ${item.id}")
//                    }
//
//                    else -> {
//                        _errorMessage.value = result.errorMessage ?: "Failed to enable item"
//                        Log.e(
//                            className,
//                            "Failed to enable Amazon Subscribe item: ${result.errorMessage}"
//                        )
//                    }
//                }
//            } catch (e: Exception) {
//                _errorMessage.value = "Unexpected error: ${e.message}"
//                Log.e(className, "Exception while enabling Amazon Subscribe item", e)
//            }
//        }
//    }
//}