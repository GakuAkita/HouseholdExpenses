package gaku.original.myapplication.viewModel.settings

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.repository.PrefKeys
import gaku.original.myapplication.repository.SharedPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class PayPayReceiptOCRSettingViewModel @Inject constructor(
    private val prefRepository: SharedPreferencesRepository
) : ViewModel() {

    private val _isLeftRatioSet = mutableStateOf(false)
    val isLeftRatioSet: State<Boolean> = _isLeftRatioSet

    private val _isTopRatioSet = mutableStateOf(false)
    val isTopRatioSet: State<Boolean> = _isTopRatioSet

    private val _leftRatio = mutableStateOf(0f)
    val leftRatio: State<Float> = _leftRatio
    private val _topRatio = mutableStateOf(0f)
    val topRatio: State<Float> = _topRatio

    init {
        getIsRatioSet()
        if (checkBothRatioSet()) {
            getRatios()
        }
    }

    fun getIsRatioSet() {
        _isLeftRatioSet.value = prefRepository.hasKey(PrefKeys.PAYPAY_RECEIPT_LEFT_MASK_RATIO)
        _isTopRatioSet.value = prefRepository.hasKey(PrefKeys.PAYPAY_RECEIPT_TOP_MASK_RATIO)
    }

    fun checkBothRatioSet(): Boolean {
        return _isLeftRatioSet.value && _isTopRatioSet.value
    }

    fun getRatios() {
        _leftRatio.value = prefRepository.getFloat(PrefKeys.PAYPAY_RECEIPT_LEFT_MASK_RATIO, 0f)
        _topRatio.value = prefRepository.getFloat(PrefKeys.PAYPAY_RECEIPT_TOP_MASK_RATIO, 0f)
    }

    fun resetRatio() {
        prefRepository.remove(PrefKeys.PAYPAY_RECEIPT_LEFT_MASK_RATIO)
        prefRepository.remove(PrefKeys.PAYPAY_RECEIPT_TOP_MASK_RATIO)
    }

}