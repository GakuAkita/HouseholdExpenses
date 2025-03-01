package gaku.original.myapplication.data.RepositoryUtil

import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.data.Interface.CommonProperty

fun <T : CommonProperty> updateDataToRTDb(
    data: T,
    getRef: () -> DatabaseReference, // データ参照を取得するための関数
    callback: (Boolean) -> Unit = {}
) {
    val dataRef = getRef()
    val id = data.id
    if (id.isNullOrEmpty()) {
        Log.e("updateDataToRTDb", "id is null or empty")
        callback(false)
        return
    }

    val updateRef = dataRef.child(data.id ?: return)

    // Save the new instance with the generated key
    updateRef.setValue(data)
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