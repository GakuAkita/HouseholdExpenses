package gaku.original.myapplication.data.repository.amazonSubscribeItem

import gaku.original.myapplication.data.dataClass.AmazonSubscribeItem

class FakeAmazonSubscribeItemRepository: AmazonSubscribeItemRepository {
    var samples = mapOf(
        "1" to AmazonSubscribeItem(
            id = "1",
            productName = "商品1",
            price = 1000.0f,
            quantity = 1,
            enabled = true
        ),
        "2" to AmazonSubscribeItem(
            id = "2",
            productName = "商品2",
            price = 2000.0f,
            quantity = 2,
            enabled = false
        )
    )

    override suspend fun getAllAmazonSubscribeItems(): Map<String, AmazonSubscribeItem> {
        return samples
    }
}