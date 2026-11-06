package gaku.original.myapplication.data.repository.rakutenCardExclude

import gaku.original.myapplication.data.dataClass.MatchCondition
import gaku.original.myapplication.data.dataClass.RakutenCardExcludeItem
import kotlinx.coroutines.delay
import java.util.UUID

class FakeRakutenCardExcludeRepository : RakutenCardExcludeRepository {

    private val excludeItems = mutableMapOf(
        "1" to RakutenCardExcludeItem(
            id = "1",
            name = "テスト店",
            condition = MatchCondition.EXACT
        ),
        "2" to RakutenCardExcludeItem(
            id = "2",
            name = "テスト",
            condition = MatchCondition.CONTAINS
        ),
    )

    override suspend fun getExcludeItems(): Map<String, RakutenCardExcludeItem> {
        delay(1000)
        return excludeItems.toMap()
    }

    override suspend fun addExcludeItem(item: RakutenCardExcludeItem) {
        delay(1000)
        val id = UUID.randomUUID().toString()
        excludeItems[id] = item.copy(id = id)
    }

    override suspend fun deleteExcludeItem(item: RakutenCardExcludeItem) {
        delay(1000)
        excludeItems.remove(item.id!!)
    }
}
