package gaku.original.myapplication//ここ抽象化してexpense用とcategory用に継承させたほうが良いのか?
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query

class DbListenerManager(
    private val realtimeDbReference: RealtimeDbReference
) {
    // DatabaseReference とリスナーのペアを保持するリスト
    private val _listeners = mutableListOf<Pair<Any, ChildEventListener>>()
    val listeners: List<Pair<Any, ChildEventListener>> get() = _listeners

    // DatabaseReferenceとQueryのどちらも使えるように、getメソッドを定義
    val expenseRef: DatabaseReference
        get() = realtimeDbReference.getUserExpenseRef()

    val categoryRef: DatabaseReference
        get() = realtimeDbReference.getUserCategoryRef()

    /**
     * リスナーを追加する (DatabaseReferenceまたはQueryに対応)
     */
    fun addListener(reference: Any, listener: ChildEventListener) {
        when (reference) {
            is DatabaseReference -> reference.addChildEventListener(listener)
            is Query -> reference.addChildEventListener(listener)
            else -> throw IllegalArgumentException("Invalid reference type")
        }
        _listeners.add(reference to listener) // リスナーと参照のペアを保存
    }

    /**
     * リスナーをリセットする (指定した参照に紐づくリスナーを削除して新しいリスナーを追加)
     */
    fun resetListener(reference: Any, listener: ChildEventListener) {
        // 指定された参照に紐づくリスナーを削除
        val toRemove = listeners.filter { it.first == reference && it.second == listener }
        toRemove.forEach {
            when (it.first) {
                is DatabaseReference -> (it.first as DatabaseReference).removeEventListener(it.second)
                is Query -> (it.first as Query).removeEventListener(it.second)
            }
            _listeners.remove(it) // リストから削除
        }
    }

    /**
     * すべてのリスナーを削除する (アプリ終了時などに呼び出す)
     */
    fun removeAllListeners() {
        listeners.forEach { (reference, listener) ->
            when (reference) {
                is DatabaseReference -> reference.removeEventListener(listener)
                is Query -> reference.removeEventListener(listener)
            }
        }
        _listeners.clear() // リストをクリア
    }
}
