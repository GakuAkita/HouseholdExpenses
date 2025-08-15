package gaku.original.myapplication.viewModel.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.FuncStatusInfo
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.data.dataClass.EmailTemplateType
import gaku.original.myapplication.repository.FirestoreRepository.CategoryFirestoreRepository
import gaku.original.myapplication.repository.RealtimeDBrepository.MailboxExtractionRTDbRepository
import gaku.original.myapplication.utility.LogAkitaDebug
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class _MailboxExtractionViewModel @Inject constructor(
    private val categoryFirestoreRepository: CategoryFirestoreRepository,
    private val mailboxExtractionRepository: MailboxExtractionRTDbRepository
) : ViewModel() {
    val className: String = this::class.simpleName ?: "UnableToGetClassName"

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> get() = _categories

    private val _mailboxExtractionSetting = MutableStateFlow<EmailTemplateType?>(null)
    val mailboxExtractionSetting: StateFlow<EmailTemplateType?> get() = _mailboxExtractionSetting

    /* MailboxExtractionViewにしか紐づけられないのでbackStackで戻れば毎回Clearされる */
    override fun onCleared() {
        super.onCleared()
        Log.d(className, "$className was Cleared!!")
    }

    init {
        Log.d(className, "$className was Initialized!!")
    }

    suspend fun fetchMailboxExtractionIternalSettingWithLocalUpdate(
        instance: EmailTemplateType
    ): FuncStatusInfo {
        val result =
            mailboxExtractionRepository.getMailTypeSetting(
                instance
            )
        if (result is FuncResultWithData.Success) {
            _mailboxExtractionSetting.value = result.data
        }
        return result.toFuncStatusInfo()
    }

    fun fetchMailboxExtractionInternalSetting(
        instance: EmailTemplateType,
        callback: (FuncStatusInfo) -> Unit
    ) {
        viewModelScope.launch {
            val ret = fetchMailboxExtractionIternalSettingWithLocalUpdate(instance)
            callback(ret)
        }
    }

    fun addCategoryAssignment(
        type: EmailTemplateType,
        assignment: CategoryAssignment,
        callback: (FuncStatusInfo) -> Unit
    ) {
//        viewModelScope.launch {
//            val result =
//                mailboxExtractionRepository.addCategoryAssignment(type, assignment)
//            if (result.status != FuncStatus.SUCCESS) {
//                callback(result)
//                return@launch
//            }
//            /* もし成功だったら、ローカルもupdate */
//            val ret = fetchMailboxExtractionIternalSettingWithLocalUpdate(type)
//            callback(ret)
//        }
    }

    fun setMailboxExtractionInternalSetting(
        instance: EmailTemplateType,
        callback: (FuncStatusInfo) -> Unit
    ) {
        LogAkitaDebug("${instance}")
        viewModelScope.launch {
            val ret = mailboxExtractionRepository.updateMailTypeSetting(
                instance
            )
            if (ret.status == FuncStatus.SUCCESS) {
                _mailboxExtractionSetting.value = instance//内部を更新
            }
            callback(ret)
        }
    }

    fun fetchCategories(callback: (FuncStatusInfo) -> Unit) {
        viewModelScope.launch {
            val fetchResult = categoryFirestoreRepository.fetchAllCategories()
            if (fetchResult is FuncResultWithData.Success) {
                _categories.value = fetchResult.data
            }
            callback(fetchResult.toFuncStatusInfo())
        }
    }

}