package gaku.original.myapplication.data.repository.repeatAdd

import com.google.android.play.integrity.internal.q
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.RepeatAdd
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