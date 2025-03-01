package gaku.original.myapplication.data

import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.RealtimeDbReference
import gaku.original.myapplication.data.RepositoryUtil.addDataToRTDb
import gaku.original.myapplication.data.RepositoryUtil.removeDataFromRTDb
import gaku.original.myapplication.data.RepositoryUtil.updateDataToRTDb
import kotlinx.coroutines.tasks.await

class RepeatAddRepository(
    private val realtimeDbReference: RealtimeDbReference
) {
    val repeatAddRef: DatabaseReference
        get() = realtimeDbReference.getUserRepeatAddRef()

    // ユーザーIDに基づいてデータをリストとして返す（非同期）
    suspend fun fetchRepeatAddSettings(
        callback: (Boolean) -> Unit = {}
    ): List<RepeatAdd>? {
        try {
            val snapshot = realtimeDbReference.getUserRepeatAddRef().get().await()
            val repeatAdds = snapshot.children.mapNotNull {
                it.getValue(RepeatAdd::class.java)
            }
            Log.d("RepeatAddRepository", "Fetched RepeatAdd: $repeatAdds")
            callback(true)
            return repeatAdds
        } catch (e: Exception) {
            Log.d("RepeatAddRepository", "fetchRepeatAddSettings failed. ${e.message}")
            callback(false)
            return null  // エラー時には空のリストを返す
        }
    }

    fun addRepeatAdd(
        repeatAdd: RepeatAdd,
        callback: (Boolean) -> Unit = {}
    ) {
        addDataToRTDb(repeatAdd, { repeatAddRef }, callback)
    }

    fun updateRepeatAdd(
        repeatAdd: RepeatAdd,
        callback: (Boolean) -> Unit = {}
    ) {
        updateDataToRTDb(repeatAdd, { repeatAddRef }, callback)
    }

    fun removeRepeatAdd(
        repeatAdd: RepeatAdd,
        callback: (Boolean) -> Unit = {}
    ) {
        removeDataFromRTDb(repeatAdd, { repeatAddRef }, callback)
    }
}