package gaku.original.myapplication.data.repository.amazonSubscribeItem

import gaku.original.myapplication.data.dataClass.AmazonSubscribeItem

interface AmazonSubscribeItemRepository {

    suspend fun getAllAmazonSubscribeItems(): Map<String, AmazonSubscribeItem>

    suspend fun updateAmazonSubscribeItem(item: AmazonSubscribeItem)
}