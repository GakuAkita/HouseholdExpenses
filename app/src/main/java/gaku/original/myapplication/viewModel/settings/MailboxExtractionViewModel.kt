package gaku.original.myapplication.viewModel.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.FetchResult
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.data.dataClass.MailboxExtractionType
import gaku.original.myapplication.repository.FirestoreRepository.CategoryFirestoreRepository
import gaku.original.myapplication.repository.RealtimeDBrepository.MailboxExtractionRTDbRepository
import gaku.original.myapplication.utility.LogAkitaDebug
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MailboxExtractionViewModel @Inject constructor(
    private val categoryFirestoreRepository: CategoryFirestoreRepository,
    private val mailboxExtractionRepository: MailboxExtractionRTDbRepository
) : ViewModel() {
    val className: String = this::class.simpleName ?: "UnableToGetClassName"

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> get() = _categories

    private val _mailboxExtractionSetting = MutableStateFlow<MailboxExtractionType?>(null)
    val mailboxExtractionSetting: StateFlow<MailboxExtractionType?> get() = _mailboxExtractionSetting

    /* MailboxExtractionViewにしか紐づけられないのでbackStackで戻れば毎回Clearされる */
    override fun onCleared() {
        super.onCleared()
        Log.d(className, "$className was Cleared!!")
    }

    init {
        Log.d(className, "$className was Initialized!!")
    }

    suspend fun fetchMailboxExtractionIternalSettingWithLocalUpdate(
        instance: MailboxExtractionType
    ): SuspendFuncStatusInfo {
        val result =
            mailboxExtractionRepository.getMailTypeSetting(
                instance
            )
        if (result is FetchResult.Success) {
            _mailboxExtractionSetting.value = result.data
        }
        return result.toSuspendFuncStatusInfo()
    }

    fun fetchMailboxExtractionInternalSetting(
        instance: MailboxExtractionType,
        callback: (SuspendFuncStatusInfo) -> Unit
    ) {
        viewModelScope.launch {
            val ret = fetchMailboxExtractionIternalSettingWithLocalUpdate(instance)
            callback(ret)
        }
    }

    fun addCategoryAssignment(
        type: MailboxExtractionType,
        assignment: CategoryAssignment,
        callback: (SuspendFuncStatusInfo) -> Unit
    ) {
        viewModelScope.launch {
            val result =
                mailboxExtractionRepository.addCategoryAssignment(type, assignment)
            if (result.status != SuspendFuncStatus.SUCCESS) {
                callback(result)
                return@launch
            }
            /* もし成功だったら、ローカルもupdate */
            val ret = fetchMailboxExtractionIternalSettingWithLocalUpdate(type)
            callback(ret)
        }
    }

    fun setMailboxExtractionInternalSetting(
        instance: MailboxExtractionType,
        callback: (SuspendFuncStatusInfo) -> Unit
    ) {
        LogAkitaDebug("${instance}")
        viewModelScope.launch {
            val ret = mailboxExtractionRepository.updateMailTypeSetting(
                instance
            )
            if (ret.status == SuspendFuncStatus.SUCCESS) {
                _mailboxExtractionSetting.value = instance//内部を更新
            }
            callback(ret)
        }
    }

    fun fetchCategories(callback: (SuspendFuncStatusInfo) -> Unit) {
        viewModelScope.launch {
            val fetchResult = categoryFirestoreRepository.fetchAllCategories()
            if (fetchResult is FetchResult.Success) {
                _categories.value = fetchResult.data
            }
            callback(fetchResult.toSuspendFuncStatusInfo())
        }
    }

}