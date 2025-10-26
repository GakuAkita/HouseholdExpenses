package gaku.original.myapplication.useCase

import android.util.Log
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.dataClass.ExpenseSearchFilter
import gaku.original.myapplication.data.dataClass.getDefaultSearchFilter
import gaku.original.myapplication.repository.SharedPreferencesRepository
import gaku.original.myapplication.repository.PrefKeys
import javax.inject.Inject

class SearchFilterUseCase @Inject constructor(
    private val sharedPreferencesRepository: SharedPreferencesRepository
) {
    companion object {
        private const val TAG = "SearchFilterUseCase"
    }

    /**
     * ExpenseSearchFilterをSharedPreferencesに保存
     */
    fun saveSearchFilter(filter: ExpenseSearchFilter): FuncResultWithData<ExpenseSearchFilter> {
        return try {
            // GeneratedTypesを保存（カンマ区切りの文字列）
            val generatedTypesStr = filter.generatedTypes?.joinToString(",") ?: ""
            sharedPreferencesRepository.setString(PrefKeys.SEARCH_FILTER_GENERATED_TYPES, generatedTypesStr)
            
            // CategoryIdsを保存（カンマ区切りの文字列、nullは"null"として保存）
            val categoryIdsStr = filter.categoryIds?.joinToString(",") { it ?: "null" } ?: ""
            sharedPreferencesRepository.setString(PrefKeys.SEARCH_FILTER_CATEGORY_IDS, categoryIdsStr)
            
            // その他のフィールドを保存（nullの場合は空文字列として保存）
            sharedPreferencesRepository.setString(PrefKeys.SEARCH_FILTER_DATE_FROM, filter.dateFrom ?: "")
            sharedPreferencesRepository.setString(PrefKeys.SEARCH_FILTER_DATE_TO, filter.dateTo ?: "")
            sharedPreferencesRepository.setString(PrefKeys.SEARCH_FILTER_STORE_NAME, filter.storeName ?: "")
            sharedPreferencesRepository.setString(PrefKeys.SEARCH_FILTER_ITEM_NAME, filter.itemName ?: "")
            sharedPreferencesRepository.setString(PrefKeys.SEARCH_FILTER_NOTE, filter.note ?: "")
            
            // 金額フィールドも同様に空文字列として保存（nullの場合は空文字列）
            sharedPreferencesRepository.setString(PrefKeys.SEARCH_FILTER_AMOUNT_MIN, filter.amountMin?.toString() ?: "")
            sharedPreferencesRepository.setString(PrefKeys.SEARCH_FILTER_AMOUNT_MAX, filter.amountMax?.toString() ?: "")
            
            Log.d(TAG, "saveSearchFilter: Saved filter to preferences: $filter")
            FuncResultWithData.Success(filter)
        } catch (e: Exception) {
            Log.e(TAG, "saveSearchFilter: Error saving filter to preferences: ${e.message}")
            FuncResultWithData.Failure.GenericFailure(
                status = gaku.original.myapplication.data.Constants.Status.FuncStatus.FAILED,
                errorMessage = e.message ?: "Unknown error occurred while saving filter"
            )
        }
    }

    /**
     * SharedPreferencesからExpenseSearchFilterを復元
     */
    fun loadSearchFilter(): FuncResultWithData<ExpenseSearchFilter> {
        return try {
            // GeneratedTypesを復元
            val generatedTypesStr = sharedPreferencesRepository.getString(PrefKeys.SEARCH_FILTER_GENERATED_TYPES, "")
            val generatedTypes = if (generatedTypesStr.isNullOrBlank()) {
                null
            } else {
                generatedTypesStr.split(",").filter { it.isNotBlank() }
            }
            
            // CategoryIdsを復元
            val categoryIdsStr = sharedPreferencesRepository.getString(PrefKeys.SEARCH_FILTER_CATEGORY_IDS, "")
            val categoryIds = if (categoryIdsStr.isNullOrBlank()) {
                null
            } else {
                categoryIdsStr.split(",").map { if (it == "null") null else it }
            }
            
            // その他のフィールドを復元
            val dateFrom = sharedPreferencesRepository.getString(PrefKeys.SEARCH_FILTER_DATE_FROM, "")?.ifBlank { null }
            val dateTo = sharedPreferencesRepository.getString(PrefKeys.SEARCH_FILTER_DATE_TO, "")?.ifBlank { null }
            val storeName = sharedPreferencesRepository.getString(PrefKeys.SEARCH_FILTER_STORE_NAME, "")?.ifBlank { null }
            val itemName = sharedPreferencesRepository.getString(PrefKeys.SEARCH_FILTER_ITEM_NAME, "")?.ifBlank { null }
            val note = sharedPreferencesRepository.getString(PrefKeys.SEARCH_FILTER_NOTE, "")?.ifBlank { null }
            
            // 金額を復元
            val amountMinStr = sharedPreferencesRepository.getString(PrefKeys.SEARCH_FILTER_AMOUNT_MIN, "")
            val amountMin = amountMinStr?.toLongOrNull()
            val amountMaxStr = sharedPreferencesRepository.getString(PrefKeys.SEARCH_FILTER_AMOUNT_MAX, "")
            val amountMax = amountMaxStr?.toLongOrNull()
            
            val filter = ExpenseSearchFilter(
                generatedTypes = generatedTypes,
                categoryIds = categoryIds,
                dateFrom = dateFrom,
                dateTo = dateTo,
                amountMin = amountMin,
                amountMax = amountMax,
                storeName = storeName,
                itemName = itemName,
                note = note
            )
            
            Log.d(TAG, "loadSearchFilter: Loaded filter from preferences: $filter")
            FuncResultWithData.Success(filter)
        } catch (e: Exception) {
            Log.e(TAG, "loadSearchFilter: Error loading filter from preferences: ${e.message}")
            FuncResultWithData.Failure.GenericFailure(
                status = gaku.original.myapplication.data.Constants.Status.FuncStatus.FAILED,
                errorMessage = e.message ?: "Unknown error occurred while loading filter"
            )
        }
    }

    /**
     * 保存されたフィルターを復元し、空の場合はデフォルトフィルターを返す
     */
    fun loadSavedFilterOrDefault(): FuncResultWithData<ExpenseSearchFilter> {
        return try {
            val result = loadSearchFilter()
            when (result) {
                is FuncResultWithData.Success -> {
                    val savedFilter = result.data
                    // 保存されたフィルターが空でない場合はそれを使用、空の場合はデフォルトを使用
                    if (!savedFilter.isEmpty()) {
                        Log.d(TAG, "loadSavedFilterOrDefault: Using saved filter: $savedFilter")
                        FuncResultWithData.Success(savedFilter)
                    } else {
                        Log.d(TAG, "loadSavedFilterOrDefault: No saved filter found, using default")
                        val defaultFilter = getDefaultSearchFilter()
                        FuncResultWithData.Success(defaultFilter)
                    }
                }
                is FuncResultWithData.Failure -> {
                    Log.e(TAG, "loadSavedFilterOrDefault: Error loading saved filter: ${result.errorMessage}, using default")
                    val defaultFilter = getDefaultSearchFilter()
                    FuncResultWithData.Success(defaultFilter)
                }
                else -> {
                    Log.e(TAG, "loadSavedFilterOrDefault: Unexpected result type: $result, using default")
                    val defaultFilter = getDefaultSearchFilter()
                    FuncResultWithData.Success(defaultFilter)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "loadSavedFilterOrDefault: Unexpected error: ${e.message}, using default")
            val defaultFilter = getDefaultSearchFilter()
            FuncResultWithData.Success(defaultFilter)
        }
    }
}
