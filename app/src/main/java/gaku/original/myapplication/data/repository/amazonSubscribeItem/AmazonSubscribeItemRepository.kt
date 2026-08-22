package gaku.original.myapplication.data.repository.amazonSubscribeItem

import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.dataClass.AmazonSubscribeItem

interface AmazonSubscribeItemRepository {

    suspend fun getAllAmazonSubscribeItems(): Map<String, AmazonSubscribeItem>


}