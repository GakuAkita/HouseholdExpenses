package gaku.original.myapplication.data.dataClass

import kotlinx.serialization.Serializable

@Serializable
data class RepeatAdd(
    val id: String,
    val timestamp: Long,/* When this RepeatAdd was registered */
    val expense: ExpenseTemplate,
    val frequencyInfo: RepeatFrequency,
    /* everyday? weekly? monthly? yearly? */
    /**
     * everyday:何時？
     * weekly:何時?
     * monthly:何日の何時？
     * yearly:何月何日の何時?
     */
)
