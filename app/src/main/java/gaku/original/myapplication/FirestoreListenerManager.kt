package gaku.original.myapplication

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import gaku.original.myapplication.Utility.fromLocalDateTime
import gaku.original.myapplication.data.Category
import gaku.original.myapplication.data.Expense
import java.time.LocalTime
import java.time.YearMonth
import javax.inject.Inject

class FirestoreListenerManager @Inject constructor(
    private val firestoreReference: FirestoreReference
) {
    // DatabaseReference とリスナーのペアを保持するリスト
    private val _listenerMap = mutableMapOf<String, ListenerRegistration>()

    /**
     * Expensesは
     * 追加された直近直後数ヶ月だけ取得
     * カレンダーのページが無限スクロールできようにするのであれば、
     * カレンダーがスクロールされるたびにリスナーを入れ替えて、、みたいなことをしないとだめだわ。
     * 考えることが多すぎる。
     */
    fun listenToExpensesModifiedRemoved(
        key: String = "expenses_modified_removed",/* 自分で設定する */
        yearMonth: YearMonth,
        monthNum: Long,/* 前後何ヶ月分検知するか */
        onModified: (Expense) -> Unit,
        onRemoved: (Expense) -> Unit
    ) {
        // 月初と月末を Timestamp に変換
        val startStr =
            fromLocalDateTime(yearMonth.minusMonths(monthNum).atDay(1).atStartOfDay())!!
        val endStr =
            fromLocalDateTime(yearMonth.plusMonths(monthNum).atEndOfMonth().atTime(LocalTime.MAX))!!


        // すでに登録されていれば削除
        _listenerMap[key]?.remove()

        val registration = firestoreReference.getExpensesColRef()?.let {
            it.whereGreaterThanOrEqualTo("datetime", startStr)
                .whereLessThanOrEqualTo("datetime", endStr)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener

                    for (change in snapshot.documentChanges) {
                        val doc = change.document
                        val expense = doc.toObject(Expense::class.java).copy(id = doc.id)
                        when (change.type) {
                            DocumentChange.Type.MODIFIED -> onModified(expense)
                            DocumentChange.Type.REMOVED -> onRemoved(expense)
                            else -> {
                                /* Addは別のリスナーを追加する。 */
                            }
                        }
                    }
                }
        }

        if (registration != null) {
            _listenerMap[key] = registration
        }

    }

    /**
     * こっちはタイムスタンプを見る!!!!
     * 古い月を見ているときはちょっとややこしくなるが、運用上は問題ない。
     * 例えば、表示月が1年前で、現在の月に対して費用が追加された時、sharedViewModelの配列には追加されてしまう。
     * ListViewModelでフィルターを掛けているからユーザーが二見られるわけではないが。
     */
    fun listenToNewExpensesOnly(
        key: String = "expenses_add",
        nowTimestamp: Long = System.currentTimeMillis(),/* nowが取れないわけがない。 */
        onAdded: (Expense) -> Unit
    ) {
        _listenerMap[key]?.remove()

        val registration = firestoreReference.getExpensesColRef()?.let {
            it.whereGreaterThan("timestamp", nowTimestamp)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener

                    for (change in snapshot.documentChanges) {
                        if (change.type == DocumentChange.Type.ADDED) {
                            val doc = change.document
                            val expense = doc.toObject(Expense::class.java).copy(id = doc.id)
                            onAdded(expense)
                        }
                    }
                }
        }

        if (registration != null) {
            _listenerMap[key] = registration
        }
    }

    fun listenToCategoriesModifiedRemoved(
        key: String = "categories_modified_removed",/* 自分で設定する */
        onModified: (Category) -> Unit,
        onRemoved: (Category) -> Unit
    ) {
        _listenerMap[key]?.remove()

        val registration = firestoreReference.getCategoriesColRef()?.let {
            it.addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                for (change in snapshot.documentChanges) {
                    val doc = change.document
                    val category = doc.toObject(Category::class.java).copy(id = doc.id)
                    when (change.type) {
                        DocumentChange.Type.MODIFIED -> onModified(category)
                        DocumentChange.Type.REMOVED -> onRemoved(category)
                        else -> {
                            /* Addは別のリスナーを追加する。 */
                        }
                    }
                }
            }

        }

        if (registration != null) {
            _listenerMap[key] = registration
        }
    }

    /**
     * こっちはExpensesとほぼ一緒だから共通化したほうがいいかもな。
     */
    fun listenToNewCategoriesOnly(
        key: String = "categories_add",
        nowTimestamp: Long = System.currentTimeMillis(),/* nowが取れないわけがない。 */
        onAdded: (Category) -> Unit
    ) {
        _listenerMap[key]?.remove()

        val registration = firestoreReference.getCategoriesColRef()?.let {
            it.whereGreaterThan("timestamp", nowTimestamp)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener

                    for (change in snapshot.documentChanges) {
                        if (change.type == DocumentChange.Type.ADDED) {
                            val doc = change.document
                            val category = doc.toObject(Category::class.java).copy(id = doc.id)
                            onAdded(category)
                        }
                    }
                }
        }

        if (registration != null) {
            _listenerMap[key] = registration
        }
    }

    fun clearAllListeners() {
        _listenerMap.values.forEach { it.remove() }
        _listenerMap.clear()
    }
}