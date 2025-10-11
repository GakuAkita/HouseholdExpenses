package gaku.original.myapplication.data.dataClass

import gaku.original.myapplication.data.Interface.HasId

data class AmazonSubscribeItem(
    override var id: String? = null,
    val productName: String? = null,
    val frequencyWeeks: Int = -1 /* FirebaseにはIntとして保存する */
) : HasId {
    val frequency: SubscriptionFrequency
        get() = SubscriptionFrequency.fromWeeks(frequencyWeeks)
}

enum class SubscriptionFrequency(val label: String, val weeks: Int) {
    EVERY_2_WEEKS("2週間ごと", 2),
    EVERY_3_WEEKS("3週間ごと", 3),
    EVERY_4_WEEKS("4週間ごと", 4),
    EVERY_1_MONTH("1ヶ月ごと", 4), // 便宜上4週間扱い
    EVERY_5_WEEKS("5週間ごと", 5),
    EVERY_6_WEEKS("6週間ごと", 6),
    EVERY_7_WEEKS("7週間ごと", 7),
    EVERY_8_WEEKS("8週間ごと", 8),
    EVERY_2_MONTHS("2ヶ月ごと", 8),
    EVERY_3_MONTHS("3ヶ月ごと", 12),
    EVERY_4_MONTHS("4ヶ月ごと", 16),
    EVERY_5_MONTHS("5ヶ月ごと", 20),
    EVERY_6_MONTHS("6ヶ月ごと", 24),
    UNKNOWN("不明", -1);

    companion object {
        /** 文字列（例："2週間ごと"）からEnumを取得する */
        fun fromLabel(label: String?): SubscriptionFrequency =
            entries.firstOrNull { it.label == label } ?: UNKNOWN

        /** 週数から最も近いFrequencyを取得（柔軟に扱いたい場合） */
        fun fromWeeks(weeks: Int): SubscriptionFrequency =
            entries.firstOrNull { it.weeks == weeks } ?: UNKNOWN
    }
}