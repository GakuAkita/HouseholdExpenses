package gaku.original.myapplication.data

import gaku.original.myapplication.data.Interface.CommonProperty

data class RepeatAdd(
    override var id: String? = null,
    override var timestamp: Long? = null,
    var expense: Expense? = null,
    var frequency: String? = null,/* everyday? weekly? monthly? yearly?*/
    var registeredTimestamp: Long? = System.currentTimeMillis(),/* when this RepeatAdd was registered */
) : CommonProperty

