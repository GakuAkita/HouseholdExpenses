package gaku.original.myapplication.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OCRViewModel @Inject constructor(
    private val sharedImageViewModel: SharedImageViewModel
) : ViewModel() {

    override fun onCleared() {
        super.onCleared()
        Log.d("OCRViewModel", "OCRViewModel Cleared!!")
        sharedImageViewModel.clearSharedImageUri()
    }
}