package gaku.original.myapplication.viewModel.shared

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.dataClass.SharedImageData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SharedImageViewModel @Inject constructor() : ViewModel() {

    private val _sharedImageData = MutableStateFlow<SharedImageData?>(null)
    val sharedImageData: StateFlow<SharedImageData?> = _sharedImageData

    /**
     * このフラグを扱わないと
     * ダブルでnavigateしている？(どこでしているのかまじでわからん)
     */
    var isMovedToOCR: Boolean = false

    fun updateSharedImageData(data: SharedImageData?) {
        _sharedImageData.value = data
    }

    fun setIsMovedToOCR(value: Boolean) {
        isMovedToOCR = value
    }

    fun clearSharedImageUri() {
        _sharedImageData.value = null
    }
}