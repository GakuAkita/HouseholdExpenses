package gaku.original.myapplication.viewModel.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Repository.FirestoreRepository.CategoryFirestoreRepository
import gaku.original.myapplication.data.Repository.RealtimeDBrepository.MailboxExtractionRTDbRepository
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.data.dataClass.MailboxExtractionCommon
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

    private val _mailboxExtractionSetting = MutableStateFlow<MailboxExtractionCommon?>(null)
    val mailboxExtractionSetting: StateFlow<MailboxExtractionCommon?> get() = _mailboxExtractionSetting

    /* MailboxExtractionViewにしか紐づけられないのでbackStackで戻れば毎回Clearされる */
    override fun onCleared() {
        super.onCleared()
        Log.d(className, "$className was Cleared!!")
    }

    init {
        Log.d(className, "$className was Initialized!!")
    }

    suspend fun fetchMailboxExtractionIternalSettingWithLocalUpdate(
        instance: MailboxExtractionCommon,
        callback: (SuspendFuncStatusInfo) -> Unit
    ) {
        val result =
            mailboxExtractionRepository.getMailTypeSetting(
                instance,
                callback = {}
            )
        if (result.status == SuspendFuncStatus.SUCCESS) {
            _mailboxExtractionSetting.value = result.data//ここで書き換える
        }
        callback(result.toSuspendFuncStatusInfo())
    }

    fun fetchMailboxExtractionInternalSetting(
        instance: MailboxExtractionCommon,
        callback: (SuspendFuncStatusInfo) -> Unit
    ) {
        viewModelScope.launch {
            fetchMailboxExtractionIternalSettingWithLocalUpdate(instance, callback)
        }
    }

    fun addCategoryAssignment(
        type: MailboxExtractionCommon,
        assignment: CategoryAssignment,
        callback: (SuspendFuncStatusInfo) -> Unit
    ) {
        viewModelScope.launch {
            val result =
                mailboxExtractionRepository.addCategoryAssignment(type, assignment, callback)
            if (result.status != SuspendFuncStatus.SUCCESS) {
                callback(result)
                return@launch
            }
            /* もし成功だったら、ローカルもupdate */
            fetchMailboxExtractionIternalSettingWithLocalUpdate(type, callback)
        }
    }

    fun setMailboxExtractionInternalSetting(
        instance: MailboxExtractionCommon,
        callback: (SuspendFuncStatusInfo) -> Unit
    ) {
        LogAkitaDebug("${instance}")
        viewModelScope.launch {
            mailboxExtractionRepository.updateMailTypeSetting(
                instance,
                callback = { statusInfo ->
                    if (statusInfo.status == SuspendFuncStatus.SUCCESS) {
                        _mailboxExtractionSetting.value = instance//内部で更新
                    }
                    callback(statusInfo)
                }
            )
        }
    }

    fun fetchCategories(callback: (SuspendFuncStatusInfo) -> Unit) {
        viewModelScope.launch {
            val fetchResult = categoryFirestoreRepository.fetchAllCategories(callback = {})
            if (fetchResult.status == SuspendFuncStatus.SUCCESS) {
                _categories.value = fetchResult.data ?: emptyList()
            }
            callback(fetchResult.toSuspendFuncStatusInfo())
        }
    }

}