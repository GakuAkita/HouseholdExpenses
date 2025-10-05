package gaku.original.myapplication.viewModel.ocr

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.repository.SharedPreferencesRepository
import gaku.original.myapplication.viewModel.shared.SharedImageViewModel

@HiltViewModel
class OCREntryViewModel(
    private val sharedImageViewModel: SharedImageViewModel,
    private val prefRepository: SharedPreferencesRepository
) : ViewModel() {

}