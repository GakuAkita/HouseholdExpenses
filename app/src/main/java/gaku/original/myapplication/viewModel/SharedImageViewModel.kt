package gaku.original.myapplication.viewModel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharedImageViewModel @Inject constructor() : ViewModel() {
    var sharedImageUri by mutableStateOf<Uri?>(null)
        private set

    var isFromShareReceiver: Boolean = false

    /**
     * このフラグを扱わないと
     * ダブルでnavigateしている？(どこでしているのかまじでわからん)
     */
    var isMovedToOCR: Boolean = false

    fun updateSharedImageUri(uri: Uri?) {
        sharedImageUri = uri
    }

    fun setIsFromShareReceiver(value: Boolean) {
        isFromShareReceiver = value
    }

    fun setIsMovedToOCR(value: Boolean) {
        isMovedToOCR = value
    }

    fun clearSharedImageUri() {
        sharedImageUri = null
        isFromShareReceiver = false
    }
}