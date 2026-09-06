package gaku.original.myapplication.viewModel.shared

/**
 * OCRやNotificationから来るときに、AddEdit画面を複数作りたいので、
 * このViewModelは画面間の受け渡しのみに集中する
 */
//class TemporaryExpenseViewModel @Inject constructor(
//
//) : ViewModel() {
//
//    /** AddEditとMainView間でのデータ受け渡し用 **/
//    // 内部状態: 常に1件以上のExpenseを保持
//    /* 基本的にExpenseは一個だけだと思うが、一応リストでも渡せるようにしておく */
//    private var _tmpExpenseList: List<Expense> = listOf(getDefaultExpense())
//
//    // 外部公開用（読み取り専用）
//    val tmpExpenseList: List<Expense> get() = _tmpExpenseList
//
//    /** 現在編集中のExpense（とりあえず最初の要素を返す） **/
//    val currentTmpExpense: Expense
//        get() = _tmpExpenseList.first()
//
//    /** Expenseを追加 **/
//    fun addTmpExpense(newExpense: Expense = getDefaultExpense()) {
//        _tmpExpenseList = _tmpExpenseList + newExpense
//    }
//
//    /** 特定インデックスのExpenseを更新 **/
//    fun updateTmpExpenseAt(index: Int, newExpense: Expense) {
//        _tmpExpenseList = _tmpExpenseList.toMutableList().apply {
//            if (index in indices) {
//                this[index] = newExpense
//            }
//        }
//    }
//
//    /** 現在のExpense（先頭）を更新（従来互換） **/
//    fun updateTmpExpense(newExpense: Expense) {
//        updateTmpExpenseAt(0, newExpense)
//    }
//
//    fun removeTmpExpenseExceptHead() {
//        _tmpExpenseList = _tmpExpenseList.take(1)
//    }
//
//    /** 特定インデックスのExpenseを削除 **/
//    fun removeTmpExpenseAt(index: Int) {
//        if (_tmpExpenseList.size > 1) { // 最低1件は残す
//            _tmpExpenseList = _tmpExpenseList.toMutableList().apply {
//                if (index in indices) {
//                    removeAt(index)
//                }
//            }
//        }
//    }
//
//    /** 全てリセット（1件のデフォルト状態に戻す） **/
//    fun resetTmpExpenseList() {
//        _tmpExpenseList = listOf(getDefaultExpense())
//    }
//
//    fun updateTmpExpenseList(list: List<Expense>) {
//        _tmpExpenseList = list
//    }
//}