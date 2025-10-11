package gaku.original.myapplication.viewModel.ocr

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.repository.PrefKeys
import gaku.original.myapplication.repository.SharedPreferencesRepository
import gaku.original.myapplication.utility.LogAkitaDebug
import gaku.original.myapplication.viewModel.shared.SharedImageViewModel
import javax.inject.Inject

@HiltViewModel
class OCREntryViewModel @Inject constructor(
    private val sharedImageViewModel: SharedImageViewModel,
    private val prefRepository: SharedPreferencesRepository
) : ViewModel() {

    /*  */
    private val _sharedImageData = sharedImageViewModel.sharedImageData
    val sharedImageData get() = _sharedImageData

    private val _isPayPayReceiptLeftRatioSet = mutableStateOf(false)
    val isPayPayReceiptLeftRatioSet get() = _isPayPayReceiptLeftRatioSet

    private val _isPayPayReceiptTopRatioSet = mutableStateOf(false)
    val isPayPayReceiptTopRatioSet get() = _isPayPayReceiptTopRatioSet

    init {
        LogAkitaDebug("This is init")
        loadIsPayPayReceiptMaskRatioSet()
    }

    fun loadIsPayPayReceiptMaskRatioSet() {
        _isPayPayReceiptLeftRatioSet.value =
            prefRepository.hasKey(PrefKeys.PAYPAY_RECEIPT_LEFT_MASK_RATIO)
        _isPayPayReceiptTopRatioSet.value =
            prefRepository.hasKey(PrefKeys.PAYPAY_RECEIPT_TOP_MASK_RATIO)
    }

}