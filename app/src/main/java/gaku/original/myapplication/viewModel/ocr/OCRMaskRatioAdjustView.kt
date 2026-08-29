package gaku.original.myapplication.viewModel.ocr

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.dataClass.SharedImageData
import gaku.original.myapplication.data.extractor.maskBitmapTopLeftArea
import gaku.original.myapplication.utility.loadBitmapFromUri
import gaku.original.myapplication.viewModel.shared.SharedImageViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * なんかOCRViewModelと近いことしてるな、、
 */
@HiltViewModel
class OCRMaskRatioAdjustViewModel @Inject constructor(
    private val sharedImageViewModel: SharedImageViewModel,
    //private val prefRepository: SharedPreferencesRepository
) : ViewModel() {

    /**
     * この画面に来た時点で設定がされていない。
     * デフォルト値を割合にあてておく
     */
    private val _leftRatio = mutableStateOf(0.21f)
    val leftRatio: State<Float> = _leftRatio

    private val _topRatio = mutableStateOf(0.17f)
    val topRatio: State<Float> = _topRatio

    private val _sharedImageData = MutableStateFlow(sharedImageViewModel.sharedImageData.value)
    val sharedImageData: StateFlow<SharedImageData?> get() = _sharedImageData

    private val _bitmapShown = MutableStateFlow<Bitmap?>(null)
    val bitmapShown: StateFlow<Bitmap?> get() = _bitmapShown

    init {
        viewModelScope.launch {
            /**
             *  sharedViewModelのuriに更新があったときに検知できるように画面が生きている間は監視しておく
             *  sharedImageは名前が全部一緒なのでcollectされない？？
             *  */
            sharedImageViewModel.sharedImageData.collect { t ->
                _sharedImageData.value = sharedImageViewModel.sharedImageData.value
            }
        }
    }

    private fun createMaskedImage(context: Context): FuncResultWithData<Bitmap> {
        val uri = _sharedImageData.value?.imageUri
            ?: return FuncResultWithData.Failure.GenericFailure(FuncStatus.FAILED, "")

        val bitmapRet = loadBitmapFromUri(context, uri)
        if (bitmapRet !is FuncResultWithData.Success) {
            return bitmapRet
        }
        val bitmap = bitmapRet.data

        /**
         * ここでマスクをする
         */
        val maskedBitmap = maskBitmapTopLeftArea(
            source = bitmap,
            widthPercent = _leftRatio.value.toDouble(),
            heightPercent = _topRatio.value.toDouble(),
        )

        return FuncResultWithData.Success(
            data = maskedBitmap
        )
    }

    fun setBitmapShown(context: Context) {
        val createRet = createMaskedImage(context)
        if (createRet is FuncResultWithData.Success) {
            _bitmapShown.value = createRet.data
        }
    }

    fun saveAdjustedRatioSetting() {
        saveRatioSetting(_leftRatio.value, _topRatio.value)
    }

    private fun saveRatioSetting(leftRatio: Float, topRatio: Float) {
//        prefRepository.setFloat(PrefKeys.PAYPAY_RECEIPT_LEFT_MASK_RATIO, leftRatio)
//        prefRepository.setFloat(PrefKeys.PAYPAY_RECEIPT_TOP_MASK_RATIO, topRatio)
    }

    fun updateLeftRatio(ratio: Float) {
        _leftRatio.value = ratio
    }

    fun updateTopRatio(ratio: Float) {
        _topRatio.value = ratio
    }

}