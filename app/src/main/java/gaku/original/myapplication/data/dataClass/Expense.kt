package gaku.original.myapplication.data.dataClass

import androidx.compose.runtime.mutableStateListOf
import gaku.original.myapplication.data.Interface.CommonProperty
import gaku.original.myapplication.utility.AppTimeZone
import gaku.original.myapplication.utility.separateStringByBars

data class Expense(
    override var id: String? = null,
    var generatedType: String? = null,//自動生成なのか手動生成なのか
    var datetime: String? = null,//ISO_LOCAL_DATE_TIME
    override var timestamp: Long? = System.currentTimeMillis(),
    var amount: Long? = null,
    var category: Category? = null,//ここCategoryのほうが良いのかな。idとnameを一緒に保存してしまう感じ
    var note: String? = null,
    var storeName: String? = null,//必要だったらいれる。
    var itemName: String? = null,//必要だったらいれる
) : CommonProperty

data class Category(
    override var id: String? = null,
    override var timestamp: Long? = System.currentTimeMillis(),
    val name: String? = null,
    val enabled: Boolean? = true
) : CommonProperty


val defaultCategory = Category(
    id = null,
    timestamp = System.currentTimeMillis(),
    name = null,
    enabled = true
)


fun getDefaultExpense(): Expense {
    return Expense(
        id = null,
        datetime = AppTimeZone.getCurrentTimeInUTCString(),
        amount = null,
        category = null,
        note = null,
        generatedType = null
    )
}

/* 使われていない？↓ */
/* firebase functions側と一致させないとまずい */
class GeneratedType {
    companion object {
        const val AUTO = "auto"
        const val MANUAL = "manual"
        const val REPEAT_ADD = "repeat_add" // 繰り返し追加で追加するやつ
        const val MAIL_EXTRACTION = "mailbox_extraction"
    }
}

fun convertGeneratedTypeToDisplay(type: String): String {
    return when (type) {
        GeneratedType.AUTO -> "自動生成"
        GeneratedType.MANUAL -> "手動生成"
        GeneratedType.REPEAT_ADD -> "繰り返し追加"
        GeneratedType.MAIL_EXTRACTION -> "メール抽出"
        else -> "不明"
    }
}

/**
 * これ増えてきたときに、どうしようか。
 * とりあえずはこのままでいいか。data classにしたほうが拡張性は高いらしい
 */
fun convertGeneratedTypeToDisplayName(generatedType: String): Pair<String, String?> {
    val parts = separateStringByBars(generatedType)
    return when (parts.size) {
        2 -> {
            val mainType = convertGeneratedTypeToDisplay(parts[0])
            val subType = convertNodeNameToMenuName(parts[1])
            mainType to subType
        }

        1 -> convertGeneratedTypeToDisplay(parts[0]) to null
        else -> "不明" to null
    }
}

/**
 * @TODO 今はEmailTemplateTypeだけど、
 * 将来的にPayPayとか他の方法で取るようになったときには
 * 共通のinterfaceを定義してそれを返り値にする。
 */
fun convertGeneratedTypeToDefaultInstance(generatedType: String): EmailTemplateType? {
    val parts = separateStringByBars(generatedType)
    val mainType = parts.getOrNull(0)/* GeneratedType */
    val subType = parts.getOrNull(1) /* nodeName */

    if (mainType == null) {
        return null
    }

    var instance: EmailTemplateType? = null
    when (mainType) {
        GeneratedType.MAIL_EXTRACTION -> {
            if (subType != null) {
                instance = getEmailTemplateTypeByNodeName(subType)
            }
            /* nodeNameに対応するinstanceを返す */
        }

        else -> {
            /* 何もしないnullのまま */
        }
    }

    return instance
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