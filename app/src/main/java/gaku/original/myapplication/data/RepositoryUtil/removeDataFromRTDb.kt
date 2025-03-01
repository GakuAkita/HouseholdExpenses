package gaku.original.myapplication.data.RepositoryUtil

import android.util.Log
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.data.Interface.CommonProperty

fun <T : CommonProperty> removeDataFromRTDb(
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
    //上でnullチェックをしているからここで返されることはない
    val removeRef = dataRef.child(data.id ?: return)

    // Save the new instance with the generated key
    removeRef.removeValue()
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