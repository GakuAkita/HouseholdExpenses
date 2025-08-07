package gaku.original.myapplication.viewModel.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.Interface.CategoryAssignNamePattern
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.data.dataClass.CategoryAssignmentData
import gaku.original.myapplication.data.dataClass.copyWithUpdatedMap
import gaku.original.myapplication.data.dataClass.getAssignmentsByNamePattern
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

    suspend fun fetchCategoryAssignmentDataWithLocalUpdate(): FuncResultWithData<CategoryAssignmentData> {
        val result = categoryAssignmentUseCase.getCategoryAssignmentData()
        if (result is FuncResultWithData.Success) {
            /**
             * リモートに何もなかった場合は、storeとproductはnullになる。
             */
            _assignmentData.value = result.data
        }
        return result
    }

    /* リモートの全データを取得してくる */
    fun fetchCategoryAssignmentData(callback: (SuspendFuncStatusInfo) -> Unit) {
        viewModelScope.launch {
            val result = fetchCategoryAssignmentDataWithLocalUpdate()
            callback(result.toSuspendFuncStatusInfo())
        }
    }

    fun addCategoryAssignment(
        assignment: CategoryAssignment,
        namePattern: CategoryAssignNamePattern,/* 店名か製品名かはこれで切り替える */
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ) {
        viewModelScope.launch {
            val currentData = _assignmentData.value
            if (currentData == null) {
                /**
                 * nullのときは、initのデータ取得に失敗している。
                 * ローカルに保存できるように将来的にするが、現在はインターネット接続がないと
                 * 追加もできないようにしておく。
                 * UI上でnullのときは追加できないようにしておくから、ここに来ることはたぶんないがな。
                 */
                callback(
                    SuspendFuncStatusInfo(
                        status = SuspendFuncStatus.FAILED,
                        errorMessage = "CategoryAssignmentData is null. Please fetch data first."
                    )
                )
                return@launch
            }

            val addRet: SuspendFuncStatusInfo =
                categoryAssignmentUseCase.addCategoryAssignmentWithCheck(
                    assignment,
                    namePattern,
                )
            if (addRet.status == SuspendFuncStatus.SUCCESS) {
                /**
                 *  本当はstoreNameかproductNameかを見て、片方だけ更新するだけでいいが、、
                 *  まあそんな頻繁に更新するものでもないから全部取ってしまおう。
                 *  */
                val fetchRet = fetchCategoryAssignmentDataWithLocalUpdate()

                var ret: SuspendFuncStatusInfo
                if (fetchRet is FuncResultWithData.Success) {
                    _assignmentData.value = fetchRet.data
                    ret = fetchRet.toSuspendFuncStatusInfo()
                } else {
                    Log.e(
                        className,
                        "Failed to fetch updated category assignment data: ${fetchRet.toSuspendFuncStatusInfo().errorMessage}"
                    )
                    ret = SuspendFuncStatusInfo(
                        status = SuspendFuncStatus.FAILED,
                        errorMessage = "データ追加には成功しましたが、その後のリモートデータ取得で失敗しました。 ${fetchRet.toSuspendFuncStatusInfo().errorMessage}"
                    )
                }
                callback(ret)
            } else {
                callback(addRet)
            }
        }
    }

    fun updateCategoryAssignment(
        assignment: CategoryAssignment,
        namePattern: CategoryAssignNamePattern,/* 店名か製品名かはこれで切り替える */
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ) {
        viewModelScope.launch {
            val currentData = _assignmentData.value
            if (currentData == null) {
                callback(
                    SuspendFuncStatusInfo(
                        status = SuspendFuncStatus.FAILED,
                        errorMessage = "CategoryAssignmentData is null. Please fetch data first."
                    )
                )
                return@launch
            }

            val updateRet: SuspendFuncStatusInfo =
                categoryAssignmentUseCase.updateCategoryAssignmentWithCheck(
                    assignment,
                    namePattern,
                )

            var ret: SuspendFuncStatusInfo = updateRet
            if (updateRet.status == SuspendFuncStatus.SUCCESS) {
                /* ローカルについてはidを見つけて、そこだけ更新 */
                val currentMap = _assignmentData.value?.copy()
                val id = assignment.id!!/* ここは大丈夫。場合によってはチェックした方が良い。 */
                val targetMap = currentData.getAssignmentsByNamePattern(namePattern)?.toMutableMap()
                if (targetMap == null) {
                    ret = SuspendFuncStatusInfo(
                        status = SuspendFuncStatus.FAILED,
                        errorMessage = "名前パターンからカテゴリー割当を取得できません"
                    )
                } else {
                    if (targetMap.containsKey(id)) {
                        // id に該当する assignment を更新
                        targetMap[id] = assignment

                        // 更新した map を元のデータ構造に戻す
                        val updatedData = currentData.copyWithUpdatedMap(namePattern, targetMap)

                        // StateFlow に反映
                        _assignmentData.value = updatedData
                    } else {
                        ret = SuspendFuncStatusInfo(
                            status = SuspendFuncStatus.FAILED,
                            errorMessage = "更新には成功しましたが、UIへの反映に失敗しました"
                        )
                    }
                }
            }
            callback(ret)
        }
    }

    fun removeCategoryAssignment(
        assignment: CategoryAssignment,
        namePattern: CategoryAssignNamePattern,/* 店名か製品名かはこれで切り替える */
        callback: (SuspendFuncStatusInfo) -> Unit = {}
    ) {
        viewModelScope.launch {
            val currentData = _assignmentData.value
            if (currentData == null) {
                callback(
                    SuspendFuncStatusInfo(
                        status = SuspendFuncStatus.FAILED,
                        errorMessage = "CategoryAssignmentData is null. Please fetch data first."
                    )
                )
                return@launch
            }

            val removeRet: SuspendFuncStatusInfo =
                categoryAssignmentUseCase.removeCategoryAssignment(
                    assignment,
                    namePattern,
                )

            var ret: SuspendFuncStatusInfo = removeRet
            if (removeRet.status == SuspendFuncStatus.SUCCESS) {
                /* ローカルについてはidを見つけて、そこだけ削除 */
                val currentMap = _assignmentData.value?.copy()
                val id = assignment.id!!/* ここは大丈夫。場合によってはチェックした方が良い。 */
                val targetMap = currentData.getAssignmentsByNamePattern(namePattern)?.toMutableMap()
                if (targetMap == null) {
                    //ここに来ることはないはず、、
                    ret = SuspendFuncStatusInfo(
                        status = SuspendFuncStatus.FAILED,
                        errorMessage = "名前パターンからカテゴリー割当を取得できません"
                    )
                } else {
                    if (targetMap.containsKey(id)) {
                        // id に該当する assignment を削除
                        targetMap.remove(id)

                        // 更新した map を元のデータ構造に戻す
                        val updatedData = currentData.copyWithUpdatedMap(namePattern, targetMap)

                        // StateFlow に反映
                        _assignmentData.value = updatedData
                    } else {
                        ret = SuspendFuncStatusInfo(
                            status = SuspendFuncStatus.FAILED,
                            errorMessage = "削除には成功しましたが、UIへの反映に失敗しました"
                        )
                    }
                }
            }
            callback(ret)
        }
    }

    fun fetchAllCategories(callback: (SuspendFuncStatusInfo) -> Unit = {}) {
        viewModelScope.launch {
            val result = categoryRepository.fetchAllCategories()
            if (result is FuncResultWithData.Success) {
                _allCategories.value = result.data
            }
            callback(result.toSuspendFuncStatusInfo())
        }
    }
}