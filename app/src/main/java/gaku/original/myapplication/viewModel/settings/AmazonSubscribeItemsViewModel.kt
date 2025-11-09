package gaku.original.myapplication.viewModel.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.Constants.Status.LoadingStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.dataClass.AmazonSubscribeItem
import gaku.original.myapplication.repository.RealtimeDBrepository.AmazonSubscribeItemsRTDbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AmazonSubscribeItemsViewModel @Inject constructor(
    private val amazonSubscribeItemsRepository: AmazonSubscribeItemsRTDbRepository
) : ViewModel() {

    private val className: String = this::class.java.simpleName

    // UI状態管理
    private val _loadingStatus = MutableStateFlow(LoadingStatus.IDLE)
    val loadingStatus: StateFlow<LoadingStatus> = _loadingStatus

    private val _amazonSubscribeItems =
        MutableStateFlow<Map<String, AmazonSubscribeItem>>(emptyMap())
    val amazonSubscribeItems: StateFlow<Map<String, AmazonSubscribeItem>> = _amazonSubscribeItems

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        // 画面起動時にデータをロード
        loadAmazonSubscribeItems()
    }

    /**
     * Amazon定期便アイテムをロード
     */
    fun loadAmazonSubscribeItems() {
        viewModelScope.launch {
            try {
                _loadingStatus.value = LoadingStatus.LOADING
                _errorMessage.value = null

                val result = amazonSubscribeItemsRepository.getAllAmazonSubscribeItems()

                when (result) {
                    is FuncResultWithData.Success -> {
                        // enabledがtrueのアイテムのみをフィルタリング
                        val enabledItems = (result.data ?: emptyMap())
                            .filter { (_, item) -> item.enabled != false }
                        _amazonSubscribeItems.value = enabledItems
                        _loadingStatus.value = LoadingStatus.SUCCESS
                        Log.d(
                            className,
                            "Successfully loaded ${enabledItems.size} Amazon Subscribe items (enabled only)"
                        )
                    }

                    is FuncResultWithData.Failure -> {
                        _errorMessage.value = result.errorMessage
                        _loadingStatus.value = LoadingStatus.ERROR
                        Log.e(
                            className,
                            "Failed to load Amazon Subscribe items: ${result.errorMessage}"
                        )
                    }

                    is FuncResultWithData.Warning -> {
                        _errorMessage.value = result.warningMessage
                        _loadingStatus.value = LoadingStatus.ERROR
                        Log.w(
                            className,
                            "Warning while loading Amazon Subscribe items: ${result.warningMessage}"
                        )
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Unexpected error: ${e.message}"
                _loadingStatus.value = LoadingStatus.ERROR
                Log.e(className, "Exception while loading Amazon Subscribe items", e)
            }
        }
    }

    /**
     * 手動リフレッシュ
     */
    fun refresh() {
        loadAmazonSubscribeItems()
    }

    /**
     * アイテムを無効化する（enabledをfalseに設定）
     */
    fun disableItem(item: AmazonSubscribeItem) {
        viewModelScope.launch {
            try {
                val result = amazonSubscribeItemsRepository.disableAmazonSubscribeItem(item)

                when (result.status) {
                    FuncStatus.SUCCESS -> {
                        // 成功時はリストを再読み込み
                        loadAmazonSubscribeItems()
                        Log.d(className, "Successfully disabled Amazon Subscribe item: ${item.id}")
                    }
                    else -> {
                        _errorMessage.value = result.errorMessage ?: "Failed to disable item"
                        Log.e(className, "Failed to disable Amazon Subscribe item: ${result.errorMessage}")
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Unexpected error: ${e.message}"
                Log.e(className, "Exception while disabling Amazon Subscribe item", e)
            }
        }
    }
}
