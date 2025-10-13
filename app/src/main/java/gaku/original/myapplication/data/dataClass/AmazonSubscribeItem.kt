package gaku.original.myapplication.data.dataClass

import gaku.original.myapplication.data.Interface.HasId

data class AmazonSubscribeItem(
    override var id: String? = null,
    val productName: String? = null,
    val quantity: Int? = null,
    val price: Float? = 0f
) : HasId