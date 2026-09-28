package gaku.original.myapplication.data.datasource.room.entity

import androidx.room.Entity
import gaku.original.myapplication.data.dataClass.Category

/**
 * A backup copy of a category kept on the device, so categories can be shown while offline.
 *
 * The database is shared by every user who signs in on this device, so rows are keyed by [userId] too.
 */
@Entity(tableName = "categories", primaryKeys = ["userId", "id"])
data class CategoryEntity(
    val userId: String,
    val id: String,
    val timestamp: Long,
    val name: String,
    val enabled: Boolean
) {
    fun toDomain(): Category = Category(
        id = id,
        timestamp = timestamp,
        name = name,
        enabled = enabled
    )
}

fun Category.toEntity(userId: String): CategoryEntity = CategoryEntity(
    userId = userId,
    id = id,
    timestamp = timestamp,
    name = name,
    enabled = enabled
)
