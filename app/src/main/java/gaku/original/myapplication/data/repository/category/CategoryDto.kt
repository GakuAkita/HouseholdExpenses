package gaku.original.myapplication.data.repository.category

import gaku.original.myapplication.data.dataClass.Category

/**
 * The shape of a category as stored in a database. Only the repositories use this class.
 *
 * Properties are nullable vars with defaults so that Firestore toObject() or Realtime DB getValue() can create it.
 * Keep the property names in sync with Firestore functions and rules.
 */
data class CategoryDto(
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

fun Category.toDto(): CategoryDto = CategoryDto(
    id = id,
    timestamp = timestamp,
    name = name,
    enabled = enabled
)
