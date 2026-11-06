package gaku.original.myapplication.data.repository.rakutenCardExclude

import gaku.original.myapplication.data.dataClass.RakutenCardExcludeItem

interface RakutenCardExcludeRepository {
    suspend fun getExcludeItems(): Map<String, RakutenCardExcludeItem>

    suspend fun addExcludeItem(item: RakutenCardExcludeItem)

    suspend fun deleteExcludeItem(item: RakutenCardExcludeItem)
}
