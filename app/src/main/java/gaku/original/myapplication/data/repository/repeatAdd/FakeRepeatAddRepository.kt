package gaku.original.myapplication.data.repository.repeatAdd

import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.RepeatAdd
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class FakeRepeatAddRepository : RepeatAddRepository {

    var sampleRepeatAdd = mapOf(
        "1" to RepeatAdd(
            "1",
            expense = Expense(
                id = null,
                amount = 300,
                category = null,
            )
        ),
        "2" to RepeatAdd(
            id = "2",
            expense = Expense(
                id = null,
                amount = 500,
                category = null,
            )
        )
    )

    private val _repeatAdds = MutableStateFlow<Map<String, RepeatAdd>>(emptyMap())
    override val repeatAdds: StateFlow<Map<String,RepeatAdd>>
        get() = _repeatAdds.asStateFlow()

    override fun startListening() {

    }

    override fun stopListening() {

    }

    override suspend fun getAllRepeatAdds(): Map<String, RepeatAdd> {
        return sampleRepeatAdd
    }

    override suspend fun addRepeatAdd(repeatAdd: RepeatAdd): RepeatAdd {
        val newRepeatAdd = repeatAdd.copy(
            id = UUID.randomUUID().toString()
        )
        sampleRepeatAdd += (newRepeatAdd.id!! to newRepeatAdd)
        return newRepeatAdd
    }

    override suspend fun updateRepeatAdd(repeatAdd: RepeatAdd): RepeatAdd {
        sampleRepeatAdd += (repeatAdd.id!! to repeatAdd)
        return repeatAdd
    }

    override suspend fun deleteRepeatAdd(id: String) {
        sampleRepeatAdd -= id
        return
    }
}