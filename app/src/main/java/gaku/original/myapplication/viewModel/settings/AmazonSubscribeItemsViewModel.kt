package gaku.original.myapplication.viewModel.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.repository.RealtimeDBrepository.AmazonSubscribeItemsRTDbRepository
import javax.inject.Inject

@HiltViewModel
class AmazonSubscribeItemsViewModel @Inject constructor(
    private val amazonSubscribeItemsRepository: AmazonSubscribeItemsRTDbRepository
) : ViewModel() {

    // ここに必要な機能を追加
}
