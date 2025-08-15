package gaku.original.myapplication.parser

import com.google.mlkit.vision.text.Text
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.getDefaultExpense
import gaku.original.myapplication.utility.AppTimeZone
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PayPayReceiptParser(
    val textOcr: Text?
) {

    fun parse(text: Text? = textOcr): FuncResultWithData<Expense> {
        val result = getDefaultExpense()
        if (text == null) {
            return FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = "text is null"
            )
        }

        return getDefaultExpense()
    }

    fun extractDate(text: String): String? {
        val dateJaRegex = """(\d{4}年\d{1,2}月\d{1,2}日\s*\d{1,2}時\d{1,2}分)""".toRegex()
        val dateEnRegex = """\d{4}/\d{1,2}/\d{1,2}\s+\d{1,2}:\d{2}""".toRegex()

        dateJaRegex.find(text)?.let {
            /* yyyy年mm月dd日 HH時MM分をLocalDateTimeに変換して、それをUTC文字列に変える */
            val formatter = DateTimeFormatter.ofPattern("yyyy年M月d日 H時m分")
            val localDateTime = LocalDateTime.parse(it.value, formatter)
            val isoStr = AppTimeZone.localDateTimeToIsoString(localDateTime)

            return isoStr
        }

        dateEnRegex.find(text)?.let {
            val formatter = DateTimeFormatter.ofPattern("yyyy/M/d/ H:m")
            val localDateTime = LocalDateTime.parse(it.value, formatter)
            val isoStr = AppTimeZone.localDateTimeToIsoString(localDateTime)
            return isoStr
        }

        /* 検知できなかった */
        return null
    }

    fun extractAmount(text: String): Long? {
        /* 日本語 */
        val amountJaRegex = """(\d{1,3}(,\d{3})*円)""".toRegex()

        /* 英語 */
        val amountEnRegex = """\d{1,3}(,\d{3})*\s*yen""".toRegex()

        amountJaRegex.find(text)?.let {
            return it.value.replace(",", "").replace("円", "").toLong()
        }

        amountEnRegex.find(text)?.let {
            return it.value.replace(",", "").replace("yen", "").toLong()
        }

        return null
    }

    fun extractStoreName(text: String) {

    }
}