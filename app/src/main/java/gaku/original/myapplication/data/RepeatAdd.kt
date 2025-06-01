package gaku.original.myapplication.data

import gaku.original.myapplication.data.Interface.CommonProperty

data class RepeatAdd(
    override var id: String? = null,
    override var timestamp: Long? = null,/* When this RepeatAdd was registered */
    val expense: Expense = getDefaultExpense(),
    val frequencyInfo: Frequency = defaultFrequency,
    /* everyday? weekly? monthly? yearly? */
    /**
     * everyday:何時？
     * weekly:何時?
     * monthly:何日の何時？
     * yearly:何月何日の何時?
     */
    /* Timezone ID, e.g., "Asia/Tokyo" */
) : CommonProperty

data class Frequency(
    val frequency: String? = null,
    val month: Int? = null,
    val day: Int? = null,//日付
    val dayOfWeek: List<Int>? = null,//曜日
    val hour: Int? = null,
    val minute: Int? = null,
)

val defaultFrequency = Frequency(
    frequency = null,
    month = null,
    day = null,
    dayOfWeek = null,
    hour = null,
    minute = null,
)

val defaultRepeatAdd = RepeatAdd(
    id = null,
    timestamp = null,
    expense = getDefaultExpense().copy(
        datetime = null,
        timestamp = null
    ),
    frequencyInfo = defaultFrequency,
)

