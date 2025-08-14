package gaku.original.myapplication.viewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SharedImageViewModel @Inject constructor() : ViewModel() {

    private val _sharedImageUri = MutableStateFlow<Uri?>(null)
    val sharedImageUri: StateFlow<Uri?> = _sharedImageUri

    var isFromShareReceiver: Boolean = false

    /**
     * このフラグを扱わないと
     * ダブルでnavigateしている？(どこでしているのかまじでわからん)
     */
    var isMovedToOCR: Boolean = false

    fun updateSharedImageUri(uri: Uri?) {
        _sharedImageUri.value = uri
    }

    fun setIsFromShareReceiver(value: Boolean) {
        isFromShareReceiver = value
    }

    fun setIsMovedToOCR(value: Boolean) {
        isMovedToOCR = value
    }

    fun clearSharedImageUri() {
        _sharedImageUri.value = null
        isFromShareReceiver = false
    }
}