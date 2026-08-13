package gaku.original.myapplication.data.repository.repeatAdd

import gaku.original.myapplication.data.dataClass.RepeatAdd

interface RepeatAddRepository {

    suspend fun getRepeatAdd():Map<String, RepeatAdd>

    suspend fun addRepeatAdd(repeatAdd: RepeatAdd): RepeatAdd

    suspend fun updateRepeatAdd(repeatAdd: RepeatAdd): RepeatAdd

    suspend fun deleteRepeatAdd(id:String)
}