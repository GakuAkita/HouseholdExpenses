package gaku.original.myapplication.data.dataClass

import android.os.Parcelable
import androidx.compose.runtime.mutableStateListOf
import gaku.original.myapplication.data.Interface.CommonProperty
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    override var id: String? = null,
    var generatedType: GeneratedType? = null,//自動生成なのか手動生成なのか
    var datetime: String? = null,//ISO_LOCAL_DATE_TIME
    override var timestamp: Long? = System.currentTimeMillis(),
    var amount: Long? = null,
    var category: Category? = null,//ここCategoryのほうが良いのかな。idとnameを一緒に保存してしまう感じ
    var note: String? = null,
    var storeName: String? = null,//必要だったらいれる。
    var itemName: String? = null,//必要だったらいれる
) : CommonProperty

@Serializable
@Parcelize
data class Category(
    override var id: String? = null,
    override var timestamp: Long? = System.currentTimeMillis(),
    val name: String? = null,
    val enabled: Boolean? = true
) : CommonProperty, Parcelable

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
            val NAME = "mail_extraction"
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

object InitialCategories {
    //サインアップ時にデフォルトで登録されるカテゴリ
    val categories = mutableStateListOf(
        Category(name = "食費"),
        Category(name = "交通費"),
        Category(name = "生活費"),
        Category(name = "電気代"),
        Category(name = "水道代"),
        Category(name = "その他"),
    )
}