package gaku.original.myapplication.data.RepositoryUtil

import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Interface.CommonProperty
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

suspend fun <T : CommonProperty> addDataToRTDb(
    data: T,
    reference: DatabaseReference, // データ参照を取得するための関数
    callback: (SuspendFuncStatus) -> Unit = {}
) {
    val newDataRef = reference.push() // Generate the unique key

    // Create a new instance of data with the generated ID
    data.id = newDataRef.key//直接上書き
    data.timestamp = System.currentTimeMillis()//時間を上書き

    // Save the new instance with the generated key
    try {
        withTimeout(2000) {
            newDataRef.setValue(data)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("addDataToRTDb", "Data added successfully")
                        callback(SuspendFuncStatus.SUCCESS)
                    } else {
                        Log.e("addDataToRTDb", "Failed to add data", task.exception)
                        throw Exception("Failed to add data at addDataToRTDb : ${task.exception}")
                    }
                }
        }
    } catch (e: TimeoutCancellationException) {
        callback(SuspendFuncStatus.TIMEOUT)
    } catch (e: Exception) {
        callback(SuspendFuncStatus.FAILED)
    }
}