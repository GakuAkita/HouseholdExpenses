package gaku.original.myapplication.data

import gaku.original.myapplication.data.Interface.CommonProperty

data class RepeatAdd(
    override var id: String? = null,
    override var timestamp: Long? = null,/* When this RepeatAdd was registered */
    var expense: Expense? = null,
    var frequency: String? = null,
    /* everyday? weekly? monthly? yearly? */
    /**
     * everyday:何時？
     * weekly:何時?
     * monthly:何日の何時？
     * yearly:何月何日の何時?
     */
) : CommonProperty

val defaultRepeatAdd = RepeatAdd(
    id = null,
    timestamp = null,
    expense = defaultExpense,
    frequency = null,
)

