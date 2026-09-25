package gaku.original.myapplication.data.repository.repeatAdd

import gaku.original.myapplication.data.dataClass.RepeatAdd
import kotlinx.coroutines.flow.StateFlow

interface RepeatAddRepository {
    fun startListening()

    fun stopListening()

    val repeatAdds: StateFlow<Map<String, RepeatAdd>>

    suspend fun getAllRepeatAdds(): Map<String, RepeatAdd>

    suspend fun addRepeatAdd(repeatAdd: RepeatAdd): RepeatAdd

    suspend fun updateRepeatAdd(repeatAdd: RepeatAdd): RepeatAdd

    suspend fun deleteRepeatAdd(id: String)
}