package gaku.original.myapplication.viewModel.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.FetchResult
import gaku.original.myapplication.data.Interface.CategoryAssignPattern
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.data.dataClass.CategoryAssignmentData
import gaku.original.myapplication.repository.FirestoreRepository.CategoryFirestoreRepository
import gaku.original.myapplication.useCase.CategoryAssignmentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryAssignmentEditViewModel @Inject constructor(
    private val categoryAssignmentUseCase: CategoryAssignmentUseCase,
    private val categoryRepository: CategoryFirestoreRepository
) : ViewModel() {
    val className = CategoryAssignmentEditViewModel::class.java.simpleName

    override fun onCleared() {
        super.onCleared()
        Log.d(className, "$className was Cleared!!")
    }

    private val _assignmentData = MutableStateFlow<CategoryAssignmentData?>(null)
    val assignmentData: StateFlow<CategoryAssignmentData?> = _assignmentData

    private val _allCategories = MutableStateFlow<List<Category>>(emptyList())
    val allCategories: StateFlow<List<Category>> = _allCategories

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private var initialized = false
    private var activeLoadingCount = 0
    fun startInit() {
        if (initialized) {
            return
        }
        Log.d(className, "$className init Start!!")
        initialized = true
        _loading.value = true

        fun onFinish() {
            activeLoadingCount--
            if (activeLoadingCount <= 0) {
                _loading.value = false

            }
        }

        activeLoadingCount++
        fetchCategoryAssignmentData {
            onFinish()
        }

        activeLoadingCount++
        fetchAllCategories {
            onFinish()
        }

    }

    /* リモートの全データを取得してくる */
    fun fetchCategoryAssignmentData(callback: (SuspendFuncStatusInfo) -> Unit) {
        viewModelScope.launch {
            val result = categoryAssignmentUseCase.getCategoryAssignmentData()
            if (result is FetchResult.Success) {
                /**
                 * リモートに何もなかった場合は、storeとproductはnullになる。
                 */
                _assignmentData.value = result.data
            }
            callback(result.toSuspendFuncStatusInfo())
        }
    }

    fun addStoreNameCategoryAssignment(
        assignment: CategoryAssignment,
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ) {
        viewModelScope.launch {
            val ret = categoryAssignmentUseCase.addCategoryAssignmentWithCheck(
                assignment,
                CategoryAssignPattern.STORE
            )
            if (ret.status == SuspendFuncStatus.SUCCESS) {
                // 成功したら、ローカルのデータも更新
                val currentData = _assignmentData.value ?: CategoryAssignmentData()
                val updatedStoreAssignments =
                    currentData.storeName?.toMutableMap() ?: mutableMapOf()
                updatedStoreAssignments[assignment.id ?: ""] = assignment
                _assignmentData.value = currentData.copy(storeName = updatedStoreAssignments)
            }
            callback(ret)
        }
    }

    fun fetchAllCategories(callback: (SuspendFuncStatusInfo) -> Unit = {}) {
        viewModelScope.launch {
            val result = categoryRepository.fetchAllCategories()
            if (result is FetchResult.Success) {
                _allCategories.value = result.data
            }
            callback(result.toSuspendFuncStatusInfo())
        }
    }
}