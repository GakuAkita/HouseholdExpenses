package gaku.original.myapplication.data.RepositoryUtil

import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Interface.CommonProperty
import kotlinx.coroutines.TimeoutCancellationException

suspend fun <T : CommonProperty> removeDataFromRTDb(
    data: T,
    reference: DatabaseReference, // データ参照を取得するための関数
    callback: (SuspendFuncStatus) -> Unit = {}
) {
    try {
        val id = data.id
        if (id.isNullOrEmpty()) {
            Log.e("updateDataToRTDb", "id is null or empty")
            throw Exception("id${id} is null or empty")
        }
        //上でnullチェックをしているからここで返されることはない
        val removeRef = reference.child(
            data.id
                ?: throw Exception("Unable to get reference.child(${data.id}) at removeDataFromRTDb")
        )

        // Save the new instance with the generated key
        removeRef.removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("addDataToRTDb", "Data added successfully")
                    callback(SuspendFuncStatus.SUCCESS)
                } else {
                    Log.e("addDataToRTDb", "Failed to add data", task.exception)
                    throw Exception("Failed to add Data ${task.exception}")
                }
            }
    } catch (e: TimeoutCancellationException) {
        callback(SuspendFuncStatus.TIMEOUT)
    } catch (e: Exception) {
        callback(SuspendFuncStatus.FAILED)
    }
}