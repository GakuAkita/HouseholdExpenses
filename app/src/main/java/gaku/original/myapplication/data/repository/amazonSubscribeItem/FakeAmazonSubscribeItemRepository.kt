package gaku.original.myapplication.data.repository.amazonSubscribeItem

import gaku.original.myapplication.data.dataClass.AmazonSubscribeItem
import kotlinx.coroutines.delay

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
        ),
        "3" to AmazonSubscribeItem(
            id = "3",
            productName = "商品3",
            price = 3000.0f,
            quantity = 3,
            enabled = false
        ),
        "4" to AmazonSubscribeItem(
            id = "4",
            productName = "商品4",
            price = 4000.0f,
            quantity = 4,
            enabled = false
        ),
        "5" to AmazonSubscribeItem(
            id = "5",
            productName = "商品5",
            price = 5000.0f,
            quantity = 5,
            enabled = false
        ),
        "6" to AmazonSubscribeItem(
            id = "6",
            productName = "商品6",
            price = 6000.0f,
            quantity = 6,
            enabled = false
        ),
        "7" to AmazonSubscribeItem(
            id = "7",
            productName = "商品7",
            price = 7000.0f,
            quantity = 7,
            enabled = false
        ),
        "8" to AmazonSubscribeItem(
            id = "8",
            productName = "商品8",
            price = 8000.0f,
            quantity = 8,
            enabled = false
        ),
        "9" to AmazonSubscribeItem(
            id = "9",
            productName = "商品9",
            price = 9000.0f,
            quantity = 9,
            enabled = false
        ),
        "10" to AmazonSubscribeItem(
            id = "10",
            productName = "商品10",
            price = 10000.0f,
            quantity = 10,
            enabled = false
        ),
        "11" to AmazonSubscribeItem(
            id = "11",
            productName = "商品11",
            price = 11000.0f,
            quantity = 11,
            enabled = false
        ),
    )


    override suspend fun getAllAmazonSubscribeItems(): Map<String, AmazonSubscribeItem> {
        return samples
    }

    override suspend fun updateAmazonSubscribeItem(
        item: AmazonSubscribeItem
    ) {
        delay(3000)
        samples = samples.toMutableMap().apply {
            this[item.id!!] = item
        }
    }
}