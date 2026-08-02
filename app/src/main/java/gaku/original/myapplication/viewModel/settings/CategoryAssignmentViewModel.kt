package gaku.original.myapplication.viewModel.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.FuncStatusInfo
import gaku.original.myapplication.data.Interface.CategoryAssignNamePattern
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.data.dataClass.CategoryAssignmentData
import gaku.original.myapplication.data.dataClass.copyWithUpdatedMap
import gaku.original.myapplication.data.dataClass.getAssignmentsByNamePattern
import gaku.original.myapplication.useCase.CategoryAssignmentUseCase
import gaku.original.myapplication.useCase.CategoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryAssignmentEditViewModel @Inject constructor(
    private val categoryAssignmentUseCase: CategoryAssignmentUseCase,
    private val categoryUseCase: CategoryUseCase
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
//    fun startInit() {
//        if (initialized) {
//            return
//        }
//        Log.d(className, "$className init Start!!")
//        initialized = true
//        _loading.value = true
//
//        fun onFinish() {
//            activeLoadingCount--
//            if (activeLoadingCount <= 0) {
//                _loading.value = false
//
//            }
//        }
//
//        activeLoadingCount++
//        fetchCategoryAssignmentData {
//            onFinish()
//        }
//
//        activeLoadingCount++
//        fetchAllCategories {
//            onFinish()
//        }
//
//    }
//
//    suspend fun fetchCategoryAssignmentDataWithLocalUpdate(timeout: Long = 10000): FuncResultWithData<CategoryAssignmentData> {
//        val result = categoryAssignmentUseCase.getCategoryAssignmentData(timeout)
//        if (result is FuncResultWithData.Success) {
//            /**
//             * リモートに何もなかった場合は、storeとproductはnullになる。
//             */
//            _assignmentData.value = result.data
//        }
//        return result
//    }
//
//    /* リモートの全データを取得してくる */
//    fun fetchCategoryAssignmentData(callback: (FuncStatusInfo) -> Unit) {
//        viewModelScope.launch {
//            val result = fetchCategoryAssignmentDataWithLocalUpdate()
//            callback(result.toFuncStatusInfo())
//        }
//    }
//
//    fun addCategoryAssignment(
//        assignment: CategoryAssignment,
//        namePattern: CategoryAssignNamePattern,/* 店名か製品名かはこれで切り替える */
//        callback: (FuncStatusInfo) -> Unit = {}
//    ) {
//        viewModelScope.launch {
//            val currentData = _assignmentData.value
//            if (currentData == null) {
//                /**
//                 * nullのときは、initのデータ取得に失敗している。
//                 * ローカルに保存できるように将来的にするが、現在はインターネット接続がないと
//                 * 追加もできないようにしておく。
//                 * UI上でnullのときは追加できないようにしておくから、ここに来ることはたぶんないがな。
//                 */
//                callback(
//                    FuncStatusInfo(
//                        status = FuncStatus.FAILED,
//                        errorMessage = "CategoryAssignmentData is null. Please fetch data first."
//                    )
//                )
//                return@launch
//            }
//
//            val addRet: FuncResultWithData<CategoryAssignment> =
//                categoryAssignmentUseCase.addCategoryAssignmentWithCheck(
//                    assignment,
//                    namePattern,
//                )
//            if (addRet is FuncResultWithData.Success) {
//                Log.d(className, "addCategoryAssignment succeeded remotely.")
//                // まずローカルの配列に追加
//                val addedAssignment = addRet.data
//                val id = addedAssignment.id
//                if (id != null) {
//                    val targetMap =
//                        currentData.getAssignmentsByNamePattern(namePattern)?.toMutableMap()
//                            ?: mutableMapOf()
//                    targetMap[id] = addedAssignment
//                    val updatedData = currentData.copyWithUpdatedMap(namePattern, targetMap)
//                    _assignmentData.value = updatedData
//                }
//
//                // 成功コールバックを即座に返す
//                callback(
//                    FuncStatusInfo(
//                        status = FuncStatus.SUCCESS,
//                        errorMessage = "データ追加には成功しました"
//                    )
//                )
//                // 裏でLocalUpdateを実行
//                val fetchRet = fetchCategoryAssignmentDataWithLocalUpdate(2000L)
//                if (fetchRet !is FuncResultWithData.Success) {
//                    Log.e(
//                        className,
//                        "Failed to fetch updated category assignment data in background: ${fetchRet.toFuncStatusInfo().errorMessage}"
//                    )
//                }
//
//            } else {
//                Log.d(
//                    className,
//                    "addCategoryAssignment failed remotely: ${addRet.toFuncStatusInfo().errorMessage}"
//                )
//                callback(addRet.toFuncStatusInfo())
//            }
//        }
//    }
//
//    fun updateCategoryAssignment(
//        assignment: CategoryAssignment,
//        namePattern: CategoryAssignNamePattern,/* 店名か製品名かはこれで切り替える */
//        callback: (FuncStatusInfo) -> Unit = {}
//    ) {
//        viewModelScope.launch {
//            val currentData = _assignmentData.value
//            if (currentData == null) {
//                callback(
//                    FuncStatusInfo(
//                        status = FuncStatus.FAILED,
//                        errorMessage = "CategoryAssignmentData is null. Please fetch data first."
//                    )
//                )
//                return@launch
//            }
//
//            val updateRet: FuncStatusInfo =
//                categoryAssignmentUseCase.updateCategoryAssignmentWithCheck(
//                    assignment,
//                    namePattern,
//                )
//
//            if (updateRet.status == FuncStatus.SUCCESS) {
//                // まずローカルの配列を更新
//                val id = assignment.id
//                if (id != null) {
//                    val targetMap =
//                        currentData.getAssignmentsByNamePattern(namePattern)?.toMutableMap()
//                    if (targetMap != null && targetMap.containsKey(id)) {
//                        // id に該当する assignment を更新
//                        targetMap[id] = assignment
//
//                        // 更新した map を元のデータ構造に戻す
//                        val updatedData = currentData.copyWithUpdatedMap(namePattern, targetMap)
//
//                        // StateFlow に反映
//                        _assignmentData.value = updatedData
//                    }
//                }
//
//                // 成功コールバックを即座に返す
//                callback(
//                    FuncStatusInfo(
//                        status = FuncStatus.SUCCESS,
//                        errorMessage = "データ更新に成功しました"
//                    )
//                )
//
//                // 裏でLocalUpdateを実行
//                val fetchRet = fetchCategoryAssignmentDataWithLocalUpdate(2000L)
//                if (fetchRet !is FuncResultWithData.Success) {
//                    Log.e(
//                        className,
//                        "Failed to fetch updated category assignment data in background: ${fetchRet.toFuncStatusInfo().errorMessage}"
//                    )
//                }
//            } else {
//                callback(updateRet)
//            }
//        }
//    }
//
//    fun removeCategoryAssignment(
//        assignment: CategoryAssignment,
//        namePattern: CategoryAssignNamePattern,/* 店名か製品名かはこれで切り替える */
//        callback: (FuncStatusInfo) -> Unit = {}
//    ) {
//        viewModelScope.launch {
//            val currentData = _assignmentData.value
//            if (currentData == null) {
//                callback(
//                    FuncStatusInfo(
//                        status = FuncStatus.FAILED,
//                        errorMessage = "CategoryAssignmentData is null. Please fetch data first."
//                    )
//                )
//                return@launch
//            }
//
//            val removeRet: FuncStatusInfo =
//                categoryAssignmentUseCase.removeCategoryAssignment(
//                    assignment,
//                    namePattern,
//                )
//
//            if (removeRet.status == FuncStatus.SUCCESS) {
//                // まずローカルの配列から削除
//                val id = assignment.id
//                if (id != null) {
//                    val targetMap =
//                        currentData.getAssignmentsByNamePattern(namePattern)?.toMutableMap()
//                    if (targetMap != null && targetMap.containsKey(id)) {
//                        // id に該当する assignment を削除
//                        targetMap.remove(id)
//
//                        // 更新した map を元のデータ構造に戻す
//                        val updatedData = currentData.copyWithUpdatedMap(namePattern, targetMap)
//
//                        // StateFlow に反映
//                        _assignmentData.value = updatedData
//                    }
//                }
//
//                // 成功コールバックを即座に返す
//                callback(
//                    FuncStatusInfo(
//                        status = FuncStatus.SUCCESS,
//                        errorMessage = "データ削除には成功しました"
//                    )
//                )
//
//                // 裏でLocalUpdateを実行
//                val fetchRet = fetchCategoryAssignmentDataWithLocalUpdate(2000L)
//                if (fetchRet !is FuncResultWithData.Success) {
//                    Log.e(
//                        className,
//                        "Failed to fetch updated category assignment data in background: ${fetchRet.toFuncStatusInfo().errorMessage}"
//                    )
//                }
//            } else {
//                callback(removeRet)
//            }
//        }
//    }
//
//    fun fetchAllCategories(callback: (FuncStatusInfo) -> Unit = {}) {
//        viewModelScope.launch {
//            val result = categoryUseCase.fetchAllCategories()
//            if (result is FuncResultWithData.Success) {
//                _allCategories.value = result.data
//            }
//            callback(result.toFuncStatusInfo())
//        }
//    }
}