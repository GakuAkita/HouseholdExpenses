package gaku.original.myapplication.ui.screens.global.settingMenu.amazonSubscribeItem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.dataClass.AmazonSubscribeItem
import gaku.original.myapplication.data.repository.amazonSubscribeItem.AmazonSubscribeItemRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class AmazonSubscribeItemUiState(
    val isLoading: Boolean = false,
    val amazonSubscribeItems: List<AmazonSubscribeItem> = emptyList(),
)

sealed interface AmazonSubscribeItemUiEffect {
    data class ShowSnackbar(
        val message: String
    ) : AmazonSubscribeItemUiEffect
}

class AmazonSubscribeItemViewModel(
    private val amazonSubscribeItemRepository: AmazonSubscribeItemRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AmazonSubscribeItemUiState())
    val uiState: StateFlow<AmazonSubscribeItemUiState> get() = _uiState

    private val _eventFlow = MutableSharedFlow<AmazonSubscribeItemUiEffect>()
    val eventFlow get() = _eventFlow.asSharedFlow()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyApplication
                val container = app.appContainer
                val session = container.sessionContainer!!
                AmazonSubscribeItemViewModel(
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
                        amazonSubscribeItems = mapData.values.toList(),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false
                    )
                }
            }
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