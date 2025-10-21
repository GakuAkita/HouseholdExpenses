package gaku.original.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import gaku.original.myapplication.data.dataClass.Category

/**
 * Room用のCategoryエンティティ
 * Firebaseから取得したCategoryをローカルDBに保存するためのデータクラス
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val timestamp: Long,
    val name: String?,
    val enabled: Boolean
)

/**
 * CategoryEntityからCategoryへの変換
 */
fun CategoryEntity.toCategory(): Category {
    return Category(
        id = this.id,
        timestamp = this.timestamp,
        name = this.name,
        enabled = this.enabled
    )
}

/**
 * CategoryからCategoryEntityへの変換
 */
fun Category.toEntity(): CategoryEntity? {
    val categoryId = this.id ?: return null
    return CategoryEntity(
        id = categoryId,
        timestamp = this.timestamp ?: System.currentTimeMillis(),
        name = this.name,
        enabled = this.enabled ?: true
    )
}

