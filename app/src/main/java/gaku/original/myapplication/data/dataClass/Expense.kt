package gaku.original.myapplication.data.dataClass

import android.os.Parcelable
import gaku.original.myapplication.common.InstantSerializer
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * A stored expense. Every property is set when the expense is created;
 * only category, note, storeName and itemName are optional.
 */
@Serializable
data class Expense(
    val id: String,
    val generatedType: GeneratedType,//自動生成なのか手動生成なのか
    @Serializable(with = InstantSerializer::class)
    val datetime: Instant,
    val timestamp: Long,/* When this expense was created */
    val amount: Long,
    val category: Category?,//ここCategoryのほうが良いのかな。idとnameを一緒に保存してしまう感じ
    val note: String?,
    val storeName: String?,//必要だったらいれる。
    val itemName: String?,//必要だったらいれる
)

@Serializable
@Parcelize
data class Category(
    val id: String,
    val timestamp: Long,/* When this category was created */
    val name: String,
    val enabled: Boolean,
) : Parcelable

@Serializable
sealed interface GeneratedType {
    /* サーバー側の関数と一致させる必要がある */
    fun toSerialized(): String

    @Serializable
    data object Manual : GeneratedType {
        const val NAME = "manual"
        override fun toSerialized(): String = NAME
    }

    @Serializable
    data class RepeatAdd(val repeatAddId: String) : GeneratedType {
        companion object {
            val NAME = "repeat_add"
        }

        override fun toSerialized(): String = "${NAME}___${repeatAddId}"
    }

    @Serializable
    data class MailExtraction(val templateTypeName: String) : GeneratedType {
        companion object {
            val NAME = "mailbox_extraction"
        }

        override fun toSerialized(): String = "${NAME}___${templateTypeName}"
    }
}

fun String.toGeneratedType(): GeneratedType {
    val parts = split("___", limit = 2)
    return when (parts[0]) {
        GeneratedType.Manual.NAME -> GeneratedType.Manual
        GeneratedType.RepeatAdd.NAME -> GeneratedType.RepeatAdd(
            repeatAddId = parts.getOrNull(1)
                ?: throw IllegalArgumentException("Invalid GeneratedType: $this")
        )

        GeneratedType.MailExtraction.NAME -> GeneratedType.MailExtraction(
            templateTypeName = parts.getOrNull(1)
                ?: throw IllegalArgumentException("Invalid GeneratedType: $this")
        )

        else -> throw IllegalArgumentException("Invalid GeneratedType: $this")
    }
}
