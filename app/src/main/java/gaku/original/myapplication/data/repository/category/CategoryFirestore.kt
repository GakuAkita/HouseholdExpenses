package gaku.original.myapplication.data.repository.category

import gaku.original.myapplication.data.dataClass.Category

/**
 * The shape of a category document in Firestore. Only the repositories use this class.
 *
 * Properties are nullable vars with defaults so that toObject() can create it.
 * Keep the property names in sync with Firestore functions and rules.
 */
data class CategoryFirestore(
    var id: String? = null,
    var timestamp: Long? = null,
    var name: String? = null,
    var enabled: Boolean? = null,
) {
    fun toDomain(): Category = Category(
        id = id ?: error("id is null"),
        timestamp = timestamp ?: error("timestamp is null"),
        name = name ?: error("name is null"),
        enabled = enabled ?: error("enabled is null")
    )
}

fun Category.toFirestore(): CategoryFirestore = CategoryFirestore(
    id = id,
    timestamp = timestamp,
    name = name,
    enabled = enabled
)
