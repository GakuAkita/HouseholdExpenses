package gaku.original.myapplication.data.dataClass

import gaku.original.myapplication.data.Interface.HasId

/**
 * It might better to separate data class to the data class for storing in Realtime Database
 */
data class AmazonSubscribeItem(
    override var id: String? = null,
    val productName: String? = null,
    val quantity: Int? = null,
    val price: Float? = 0f,
    val timestamp: Long? = null,
    val enabled: Boolean? = true  // デフォルトは有効
) : HasId