package gaku.original.myapplication.viewModel.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.Constants.Status.LoadingStatus
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

    private val _amazonSubscribeItems = MutableStateFlow<Map<String, AmazonSubscribeItem>>(emptyMap())
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
                
                when (result.status) {
                    FuncStatus.SUCCESS -> {
                        _amazonSubscribeItems.value = result.data ?: emptyMap()
                        _loadingStatus.value = LoadingStatus.SUCCESS
                        Log.d(className, "Successfully loaded ${result.data?.size ?: 0} Amazon Subscribe items")
                    }
                    FuncStatus.FAILED -> {
                        _errorMessage.value = result.errorMessage ?: "Failed to load Amazon Subscribe items"
                        _loadingStatus.value = LoadingStatus.ERROR
                        Log.e(className, "Failed to load Amazon Subscribe items: ${result.errorMessage}")
                    }
                    FuncStatus.TIMEOUT -> {
                        _errorMessage.value = "Request timeout. Please try again."
                        _loadingStatus.value = LoadingStatus.ERROR
                        Log.e(className, "Timeout while loading Amazon Subscribe items")
                    }
                    else -> {
                        _errorMessage.value = "Unknown error occurred"
                        _loadingStatus.value = LoadingStatus.ERROR
                        Log.e(className, "Unknown error while loading Amazon Subscribe items")
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
     * エラーメッセージをクリア
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
