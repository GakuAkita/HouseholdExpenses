package gaku.original.myapplication.ui.screens.global.settingMenu.paypayReceiptReader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.repository.paypayReceipt.PayPayReceiptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class PayPayReceiptReaderUiState(
    val isLoading: Boolean = false,
    val message:String? = null,
    val topRatio: Float? = null,
    val leftRatio: Float? = null,
    val isLoadError: Boolean = false
)

class PayPayReceiptReaderViewModel(
    private val payPayReceiptRepository: PayPayReceiptRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PayPayReceiptReaderUiState())
    val uiState get() = _uiState.asStateFlow()

    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyApplication
                val container = app.appContainer
                val session = container.sessionContainer!!
                PayPayReceiptReaderViewModel(
                    session.payPayReceiptRepository
                )
            }
        }
    }

    init {
        Timber.d("Created. ${hashCode()}")

        viewModelScope.launch {
            try{
                _uiState.update {
                    it.copy(
                        isLoading = true
                    )
                }
                val topRatio = payPayReceiptRepository.getMaskTopRatio()
                val leftRatio = payPayReceiptRepository.getMaskLeftRatio()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        topRatio = topRatio,
                        leftRatio = leftRatio
                    )
                }
            }catch (e:Exception){
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

    fun onMessageShown(){
        _uiState.value = _uiState.value.copy(
            message = null
        )
    }

    override fun onCleared() {
        Timber.d("Cleared. ${hashCode()}")
        super.onCleared()
    }
}

//@HiltViewModel
//class PayPayReceiptReaderViewModel @Inject constructor(
//    private val prefRepository: SharedPreferencesRepository
//) : ViewModel() {
//
//    private val _isLeftRatioSet = mutableStateOf(false)
//    val isLeftRatioSet: State<Boolean> = _isLeftRatioSet
//
//    private val _isTopRatioSet = mutableStateOf(false)
//    val isTopRatioSet: State<Boolean> = _isTopRatioSet
//
//    private val _leftRatio = mutableStateOf(0f)
//    val leftRatio: State<Float> = _leftRatio
//    private val _topRatio = mutableStateOf(0f)
//    val topRatio: State<Float> = _topRatio
//
//    init {
//        getIsRatioSet()
//        if (checkBothRatioSet()) {
//            getRatios()
//        }
//    }
//
//    fun getIsRatioSet() {
//        _isLeftRatioSet.value = prefRepository.hasKey(PrefKeys.PAYPAY_RECEIPT_LEFT_MASK_RATIO)
//        _isTopRatioSet.value = prefRepository.hasKey(PrefKeys.PAYPAY_RECEIPT_TOP_MASK_RATIO)
//    }
//
//    fun checkBothRatioSet(): Boolean {
//        return _isLeftRatioSet.value && _isTopRatioSet.value
//    }
//
//    fun getRatios() {
//        _leftRatio.value = prefRepository.getFloat(PrefKeys.PAYPAY_RECEIPT_LEFT_MASK_RATIO, 0f)
//        _topRatio.value = prefRepository.getFloat(PrefKeys.PAYPAY_RECEIPT_TOP_MASK_RATIO, 0f)
//    }
//
//    fun resetRatio() {
//        prefRepository.remove(PrefKeys.PAYPAY_RECEIPT_LEFT_MASK_RATIO)
//        prefRepository.remove(PrefKeys.PAYPAY_RECEIPT_TOP_MASK_RATIO)
//    }
//
//}