import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference

class ListenerManager {
    // DatabaseReference とリスナーのペアを保持するリスト
    private val listeners = mutableListOf<Pair<DatabaseReference, ChildEventListener>>()

    /**
     * リスナーを追加する
     */
    fun addListener(databaseReference: DatabaseReference, listener: ChildEventListener) {
        databaseReference.addChildEventListener(listener)
        listeners.add(databaseReference to listener) // ペアとして保存
    }

    /**
     * リスナーをリセットする (指定したDatabaseReferenceに紐づくリスナーを削除して新しいリスナーを追加)
     */
    fun resetListener(databaseReference: DatabaseReference, newListener: ChildEventListener) {
        // 指定されたDatabaseReferenceに紐づくリスナーを削除
        val toRemove = listeners.filter { it.first == databaseReference }
        toRemove.forEach {
            it.first.removeEventListener(it.second) // リスナーを削除
            listeners.remove(it) // リストから削除
        }

        // 新しいリスナーを追加
        addListener(databaseReference, newListener)
    }

    /**
     * すべてのリスナーを削除する (アプリ終了時などに呼び出す)
     */
    fun removeAllListeners() {
        listeners.forEach { (databaseReference, listener) ->
            databaseReference.removeEventListener(listener)
        }
        listeners.clear() // リストをクリア
    }
}
