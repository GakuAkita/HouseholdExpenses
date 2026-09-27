package gaku.original.myapplication.data.repository.repeatAdd

import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.ExpenseTemplate
import gaku.original.myapplication.data.dataClass.RepeatAdd
import gaku.original.myapplication.data.dataClass.RepeatFrequency
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeRepeatAddRepository : RepeatAddRepository {

    var sampleRepeatAdd = mapOf(
        "1" to RepeatAdd(
            id = "1",
            timestamp = 0L,
            expense = ExpenseTemplate(
                amount = 300,
                category = Category(id = "category1", timestamp = 0L, name = "食費", enabled = true),
            ),
            frequencyInfo = RepeatFrequency.EveryMonth(day = 25, hour = 9, minute = 0)
        ),
        "2" to RepeatAdd(
            id = "2",
            timestamp = 0L,
            expense = ExpenseTemplate(
                amount = 500,
                category = Category(id = "category1", timestamp = 0L, name = "食費", enabled = true),
            ),
            frequencyInfo = RepeatFrequency.EveryMonth(day = 25, hour = 9, minute = 0)
        )
    )

    private val _repeatAdds = MutableStateFlow<Map<String, RepeatAdd>>(emptyMap())
    override val repeatAdds: StateFlow<Map<String, RepeatAdd>>
        get() = _repeatAdds.asStateFlow()

    override fun startListening() {
        _repeatAdds.value = sampleRepeatAdd
    }

    override fun stopListening() {

    }

    override suspend fun getAllRepeatAdds(): Map<String, RepeatAdd> {
        delay(2000)
        return sampleRepeatAdd
    }

    override suspend fun addRepeatAdd(repeatAdd: RepeatAdd): RepeatAdd {
        delay(2000)
        sampleRepeatAdd += (repeatAdd.id to repeatAdd)
        _repeatAdds.value = sampleRepeatAdd
        return repeatAdd
    }

    override suspend fun updateRepeatAdd(repeatAdd: RepeatAdd): RepeatAdd {
        delay(2000)
        sampleRepeatAdd += (repeatAdd.id to repeatAdd)
        _repeatAdds.value = sampleRepeatAdd
        return repeatAdd
    }

    override suspend fun deleteRepeatAdd(id: String) {
        delay(2000)
        sampleRepeatAdd -= id
        _repeatAdds.value = sampleRepeatAdd
        return
    }
}