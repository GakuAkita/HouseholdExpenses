package gaku.original.myapplication.viewModel.ocr

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.repository.SharedPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class OCRMaskRatioAdjustViewModel @Inject constructor(
    private val prefRepository: SharedPreferencesRepository
) : ViewModel() {

}