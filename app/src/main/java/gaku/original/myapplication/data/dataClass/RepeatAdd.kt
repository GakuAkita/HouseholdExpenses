package gaku.original.myapplication.data.dataClass

import android.os.Parcelable
import gaku.original.myapplication.data.Interface.CommonProperty
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
data class RepeatAdd(
    override var id: String? = null,
    override var timestamp: Long? = null,/* When this RepeatAdd was registered */
    val expense: Expense = Expense(),
    val frequencyInfo: RepeatFrequency? = null,
    /* everyday? weekly? monthly? yearly? */
    /**
     * everyday:何時？
     * weekly:何時?
     * monthly:何日の何時？
     * yearly:何月何日の何時?
     */
    /* Timezone ID, e.g., "Asia/Tokyo" */
) : CommonProperty

/* This should be moved to Firestore!!! */
@Serializable
@Parcelize
data class Frequency(
    /* This is only Firestore!! */
    val frequency: String? = null,
    val month: Int? = null,
    val day: Int? = null,//日付
    val dayOfWeek: List<Int>? = null,//曜日
    val hour: Int? = null,
    val minute: Int? = null,
) : Parcelable