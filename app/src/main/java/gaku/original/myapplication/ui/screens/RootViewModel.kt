package gaku.original.myapplication.ui.screens

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gaku.original.myapplication.MyApplication
import gaku.original.myapplication.data.Constants.ShareIntentKeys
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.repository.auth.AuthRepository
import gaku.original.myapplication.domain.AuthState
import gaku.original.myapplication.ui.screens.receiver.shareReceiver.SentData
import gaku.original.myapplication.utility.getParcelableExtraCompat
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

sealed interface RootUiEffect {

    data class ShowToast(
        val message: String
    ) : RootUiEffect

    data class ExpenseAdd(
        val expense: Expense
    ) : RootUiEffect
}

class RootViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<RootUiEffect>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val _authState = authRepository.authState
    val authState get() = _authState

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MyApplication
                val authRepository = app.appContainer.authRepository
                RootViewModel(authRepository)
            }
        }
    }

    init {
        Timber.d("Created: ${hashCode()}")
    }

    fun onNewIntent(intent: Intent?) {
        // https://developer.android.com/guide/components/activities/parcelables-and-bundles
        val sentData = intent?.getParcelableExtraCompat<SentData>(ShareIntentKeys.EXPENSE)
        Timber.d("sentData = ${sentData}")

        if (sentData != null) {
            viewModelScope.launch {
                val state = authState.first {
                    it !is AuthState.Loading
                }

                if (state is AuthState.LoggedIn) {
                    when (sentData) {
                        is SentData.Expense -> {
                            _eventFlow.emit(
                                RootUiEffect.ExpenseAdd(
                                    sentData.toExpense()
                                )
                            )
                        }
                    }
                } else if (state is AuthState.LoggedOut) {
                    // The original design is that before MainActivity is launched by ShareReceiverActivity,
                    // the user should be logged in.
                    _eventFlow.emit(
                        RootUiEffect.ShowToast("Error: Not logged in.")
                    )
                }
            }
        }
    }

    override fun onCleared() {
        Timber.d("onCleared() called. ${hashCode()}")
        super.onCleared()
    }
}

fun SentData.Expense.toExpense(): Expense {
    return Expense(
        id = null,
        datetime = datetime,
        amount = amount,
        storeName = storeName
    )
}