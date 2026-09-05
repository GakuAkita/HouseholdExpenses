package gaku.original.myapplication.ui.screens.receiver.paypayReceiptMask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.repository.paypayReceipt.PayPayReceiptConfigRepository

class PayPayReceiptMaskRatioAdjustViewModel(
    private val payPayReceiptConfigRepository: PayPayReceiptConfigRepository
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyApplication
                val container = app.appContainer
                val session = container.sessionContainer!!
                PayPayReceiptMaskRatioAdjustViewModel(
                    payPayReceiptConfigRepository = session.payPayReceiptConfigRepository
                )
            }
        }
    }

    init {

    }
}