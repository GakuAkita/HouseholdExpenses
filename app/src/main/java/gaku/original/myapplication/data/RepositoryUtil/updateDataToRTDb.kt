package gaku.original.myapplication.data.RepositoryUtil

import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Interface.CommonProperty
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

suspend fun <T : CommonProperty> updateDataToRTDb(
    data: T,
    reference: DatabaseReference, // データ参照を取得するための関数
    callback: (SuspendFuncStatus) -> Unit = {}
) {

    try {
        withTimeout(2000) {
            val id = data.id
            if (id.isNullOrEmpty()) {
                Log.e("updateDataToRTDb", "id is null or empty")
                throw Exception("id${id} is null or empty at updateDataToRTDb")
            }

            val updateRef = reference.child(
                data.id
                    ?: throw Exception("Unable to get reference.child(${data.id}) at updateDataToRTDb")
            )

            // Save the new instance with the generated key
            updateRef.setValue(data)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("addDataToRTDb", "Data added successfully")
                        callback(SuspendFuncStatus.SUCCESS)
                    } else {
                        Log.e("addDataToRTDb", "Failed to add data", task.exception)
                        callback(SuspendFuncStatus.FAILED)
                    }
                }
        }
    } catch (e: TimeoutCancellationException) {
        callback(SuspendFuncStatus.TIMEOUT)
    } catch (e: Exception) {
        callback(SuspendFuncStatus.FAILED)
    }
}