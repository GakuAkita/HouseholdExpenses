package gaku.original.myapplication.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Constants.AppPackageNames
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.FuncStatusInfo
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.NotificationData
import gaku.original.myapplication.viewModel.shared.SharedNotificationListenerViewModel
import gaku.original.myapplication.viewModel.shared.TemporaryExpenseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionInfo(
    val amount: Long?,
    val storeName: String?
)

@HiltViewModel
class NotificationListenerProcessViewModel @Inject constructor(
    private val sharedNotificationListenerViewModel: SharedNotificationListenerViewModel,
    private val tmpExpenseViewModel: TemporaryExpenseViewModel
) : ViewModel() {

    override fun onCleared() {
        super.onCleared()
        clearNotificationData()
    }

    private val _notificationData =
        MutableStateFlow(sharedNotificationListenerViewModel.getNotificationData())
    val notificationData: StateFlow<NotificationData?> get() = _notificationData

    init {
        viewModelScope.launch {
            sharedNotificationListenerViewModel.notificationData.collect {
                _notificationData.value = it
            }
        }
    }

    /**
     * UIからはこいつが呼ばれる
     */
    fun passExpenseFromNotificationData(callback: (FuncStatusInfo) -> Unit) {
        if (_notificationData.value == null) {
            return
        }

        val data = _notificationData.value
        val createResult = createExpenseFromNotificationData(data!!)
        if (createResult is FuncResultWithData.Success) {
            val expense = createResult.data
            copyReadExpenseToTmpExpense(expense)
        }
        callback(createResult.toFuncStatusInfo())
    }

    fun createExpenseFromNotificationData(data: NotificationData): FuncResultWithData<Expense> {
        val packageName = data.packageName

        when (packageName) {
            AppPackageNames.PAYPAY -> {
                val transactionInfo = extractPayPayTransactionInfo(data.text?.toString() ?: "")
                if (transactionInfo.amount != null) {
                    val expense = Expense(
                        amount = transactionInfo.amount,
                        storeName = transactionInfo.storeName
                    )
                    return FuncResultWithData.Success(expense)
                } else {
                    return FuncResultWithData.Failure.GenericFailure(
                        status = FuncStatus.FAILED,
                        errorMessage = "Failed to extract amount from PayPay notification"
                    )
                }
            }

            else -> {
                return FuncResultWithData.Failure.GenericFailure(
                    status = FuncStatus.FAILED,
                    errorMessage = "Unsupported package name: $packageName"
                )
            }
        }
    }


    fun extractPayPayTransactionInfo(text: String): TransactionInfo {
        // 金額を抽出（「3,186円」の形式）
        val amountRegex = "金額：([\\d,]+)円".toRegex()
        val amountMatch = amountRegex.find(text)
        val amount = amountMatch?.groups?.get(1)?.value?.replace(",", "")?.toLongOrNull()

        // 店舗名を抽出
        val storeRegex = "店舗名：(.+)".toRegex()
        val storeMatch = storeRegex.find(text)
        val storeName = storeMatch?.groups?.get(1)?.value?.trim()

        return TransactionInfo(amount, storeName)
    }

    fun copyReadExpenseToTmpExpense(expense: Expense) {
        tmpExpenseViewModel.resetTmpExpenseList()
        tmpExpenseViewModel.updateTmpExpense(expense)
    }

    fun clearNotificationData() {
        sharedNotificationListenerViewModel.clearNotificationData()
        //sharedNotificationListenerViewModel.setIsMovedToNLProcess(false)/* 念の為つけていてもいいかも */
    }
}