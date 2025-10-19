package gaku.original.myapplication.repository.RealtimeDBrepository

import gaku.original.myapplication.RealtimeDbReference
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.FuncStatusInfo
import gaku.original.myapplication.data.dataClass.AmazonSubscribeItem
import gaku.original.myapplication.utility.LogClassFuncCalled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import removeDataFromRTDb
import javax.inject.Inject

class AmazonSubscribeItemsRTDbRepository @Inject constructor(
    private val realtimeDbReference: RealtimeDbReference
) {
    private val className: String = this::class.simpleName ?: "UnableToGetClassName"

    /**
     * すべてのAmazon定期便アイテムを取得する（Map形式）
     */
    suspend fun getAllAmazonSubscribeItems(
        timeout: Long = 10000
    ): FuncResultWithData<Map<String, AmazonSubscribeItem>> {
        LogClassFuncCalled(className, ::getAllAmazonSubscribeItems.name)

        val refResult = realtimeDbReference.getAmazonSubscribeMonitorItemsRef()
        if (refResult !is FuncResultWithData.Success) {
            return FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = when (refResult) {
                    is FuncResultWithData.Failure -> "Failed to get Amazon Subscribe Monitor Items reference: ${refResult.errorMessage}"
                    is FuncResultWithData.Warning -> "Failed to get Amazon Subscribe Monitor Items reference: ${refResult.warningMessage}"
                    else -> "Failed to get Amazon Subscribe Monitor Items reference: Unknown error"
                }
            )
        }

        val ref = refResult.data

        return try {
            withTimeout(timeout) {
                withContext(Dispatchers.IO) {
                    val snapshot = ref.get().await()
                    val itemsMap = mutableMapOf<String, AmazonSubscribeItem>()

                    for (childSnapshot in snapshot.children) {
                        val item = childSnapshot.getValue(AmazonSubscribeItem::class.java)
                        val itemId = childSnapshot.key
                        if (item != null && itemId != null) {
                            // IDを設定
                            val itemWithId = item.copy(id = itemId)
                            itemsMap[itemId] = itemWithId
                        }
                    }

                    FuncResultWithData.Success(data = itemsMap)
                }
            }
        } catch (e: TimeoutCancellationException) {
            FuncResultWithData.Failure.Timeout()
        } catch (e: Exception) {
            FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = e.message ?: "Unknown error occurred"
            )
        }
    }

    /**
     * 特定のAmazon定期便アイテムを取得する
     */
    suspend fun getAmazonSubscribeItem(
        itemId: String,
        timeout: Long = 5000
    ): FuncResultWithData<AmazonSubscribeItem> {
        LogClassFuncCalled(className, ::getAmazonSubscribeItem.name)

        val refResult = realtimeDbReference.getAmazonSubscribeMonitorItemsRef()
        if (refResult !is FuncResultWithData.Success) {
            return FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = when (refResult) {
                    is FuncResultWithData.Failure -> "Failed to get Amazon Subscribe Monitor Items reference: ${refResult.errorMessage}"
                    is FuncResultWithData.Warning -> "Failed to get Amazon Subscribe Monitor Items reference: ${refResult.warningMessage}"
                    else -> "Failed to get Amazon Subscribe Monitor Items reference: Unknown error"
                }
            )
        }

        val ref = refResult.data

        return try {
            withTimeout(timeout) {
                withContext(Dispatchers.IO) {
                    val snapshot = ref.child(itemId).get().await()
                    if (snapshot.exists()) {
                        val item = snapshot.getValue(AmazonSubscribeItem::class.java)
                        if (item != null) {
                            val itemWithId = item.copy(id = snapshot.key)
                            FuncResultWithData.Success(data = itemWithId)
                        } else {
                            FuncResultWithData.Failure.GenericFailure(
                                status = FuncStatus.FAILED,
                                errorMessage = "Failed to convert snapshot to AmazonSubscribeItem"
                            )
                        }
                    } else {
                        FuncResultWithData.Failure.GenericFailure(
                            status = FuncStatus.FAILED,
                            errorMessage = "Document not found"
                        )
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            FuncResultWithData.Failure.Timeout()
        } catch (e: Exception) {
            FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = e.message ?: "Unknown error occurred"
            )
        }
    }

    /**
     * Amazon定期便アイテムを削除する
     */
    suspend fun removeAmazonSubscribeItem(
        item: AmazonSubscribeItem,
        timeout: Long = 3000
    ): FuncStatusInfo {
        LogClassFuncCalled(className, ::removeAmazonSubscribeItem.name)

        val refResult = realtimeDbReference.getAmazonSubscribeMonitorItemsRef()
        if (refResult !is FuncResultWithData.Success) {
            return FuncStatusInfo(
                FuncStatus.FAILED,
                when (refResult) {
                    is FuncResultWithData.Failure -> "Failed to get Amazon Subscribe Monitor Items reference: ${refResult.errorMessage}"
                    is FuncResultWithData.Warning -> "Failed to get Amazon Subscribe Monitor Items reference: ${refResult.warningMessage}"
                    else -> "Failed to get Amazon Subscribe Monitor Items reference: Unknown error"
                }
            )
        }

        val ref = refResult.data

        return removeDataFromRTDb(item, ref, timeout)
    }

}
