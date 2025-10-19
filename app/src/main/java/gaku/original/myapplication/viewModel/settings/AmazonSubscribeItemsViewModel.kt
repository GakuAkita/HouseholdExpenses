package gaku.original.myapplication.viewModel.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
                        _amazonSubscribeItems.value = result.data ?: emptyMap()
                        _loadingStatus.value = LoadingStatus.SUCCESS
                        Log.d(
                            className,
                            "Successfully loaded ${result.data.size ?: 0} Amazon Subscribe items"
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
}
