import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import gaku.original.myapplication.RealtimeDbReference

class DbListenerManager(
    private val realtimeDbReference: RealtimeDbReference
) {
    // DatabaseReference とリスナーのペアを保持するリスト
    private val listeners = mutableListOf<Pair<DatabaseReference, ChildEventListener>>()

    private val expenseRef : DatabaseReference
        get() = realtimeDbReference.getUserExpenseRef()

    /**
     * リスナーを追加する
     */
    private fun addListener(databaseReference: DatabaseReference, listener: ChildEventListener) {
        databaseReference.addChildEventListener(listener)
        listeners.add(databaseReference to listener) // ペアとして保存
    }

    /**
     * リスナーをリセットする (指定したDatabaseReferenceに紐づくリスナーを削除して新しいリスナーを追加)
     */
    private fun resetListener(databaseReference: DatabaseReference, listener: ChildEventListener) {
        // 指定されたDatabaseReferenceに紐づくリスナーを削除
        val toRemove = listeners.filter { it.first == databaseReference && it.second == listener }
        toRemove.forEach {
            it.first.removeEventListener(it.second) // リスナーを削除
            listeners.remove(it) // リストから削除
        }
    }

    /******** Expense専用 **********/
    fun addExpenseListener(listener: ChildEventListener){
        addListener(expenseRef,listener)
    }

    fun resetExpenseListener(listener:ChildEventListener){
        resetListener(expenseRef,listener)
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
