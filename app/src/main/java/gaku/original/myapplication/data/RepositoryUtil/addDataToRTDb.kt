package gaku.original.myapplication.data.RepositoryUtil

import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.data.Interface.CommonProperty

fun <T : CommonProperty> addDataToRTDb(
    data: T,
    getRef: () -> DatabaseReference, // データ参照を取得するための関数
    callback: (Boolean) -> Unit = {}
) {
    val dataRef = getRef()
    val newDataRef = dataRef.push() // Generate the unique key

    // Create a new instance of data with the generated ID
    data.id = newDataRef.key//直接上書き
    data.timestamp = System.currentTimeMillis()//時間を上書き

    // Save the new instance with the generated key
    newDataRef.setValue(data)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("addDataToRTDb", "Data added successfully")
                callback(true)
            } else {
                Log.e("addDataToRTDb", "Failed to add data", task.exception)
                callback(false)
            }
        }
}