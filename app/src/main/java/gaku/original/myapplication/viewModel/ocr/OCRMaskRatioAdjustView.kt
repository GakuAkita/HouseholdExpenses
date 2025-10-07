package gaku.original.myapplication.viewModel.ocr

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.dataClass.SharedImageData
import gaku.original.myapplication.repository.SharedPreferencesRepository
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
    private val prefRepository: SharedPreferencesRepository
) : ViewModel() {

    /**
     * この画面に来た時点で設定がされていない。
     * デフォルト値を割合にあてておく
     */
    private val _leftRatio = mutableStateOf(0.2f)
    val leftRatio: State<Float?> = _leftRatio

    private val _topRatio = mutableStateOf(0.2f)
    val topRatio: State<Float?> = _topRatio

    private val _sharedImageData = MutableStateFlow(sharedImageViewModel.sharedImageData.value)
    val sharedImageData: StateFlow<SharedImageData?> get() = _sharedImageData

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

    fun getImageUri(): Uri? {
        return sharedImageViewModel.sharedImageData.value?.imageUri
    }

}