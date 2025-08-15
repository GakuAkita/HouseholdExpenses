package gaku.original.myapplication.parser

import android.util.Log
import com.google.mlkit.vision.text.Text
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.getDefaultExpense
import gaku.original.myapplication.utility.AppTimeZone
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min

class PayPayReceiptParser(
    val textOcr: Text?
) {

    fun parse(text: Text? = textOcr): FuncResultWithData<Expense> {
        val result = getDefaultExpense()
        val t = text
        if (t == null) {
            return FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = "text is null"
            )
        }

        if (t.textBlocks.isEmpty()) {
            return FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = "textBlocks is empty"
            )
        }

        /* 見かけの幅を算出 */
        // --- 1) 全体の左右範囲（見かけの幅）を算出 ---
        var minX = Int.MAX_VALUE
        var maxX = Int.MIN_VALUE
        for (b in text.textBlocks) {
            b.boundingBox?.let { bb ->
                minX = min(minX, bb.left)
                maxX = max(maxX, bb.right)
            }
        }
        if (minX == Int.MAX_VALUE || maxX == Int.MIN_VALUE) {
            return FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = "OCR:画面幅を取得できませんでした"
            )
        }
        val leftExclusionX = minX + ((maxX - minX) * 0.21f) // 左10%

        // --- 2) ブロックを上→下（左→右）でソート ---
        /* OCRは必ずしも上から順番に呼んでくれているわけではないらしい */
        val blocks = t.textBlocks.sortedWith(
            compareBy(
                { it.boundingBox?.top ?: Int.MAX_VALUE },
                { it.boundingBox?.left ?: Int.MAX_VALUE },
            )
        )


        // --- 3) まず日付を特定 ---
        var dateIso: String? = null
        var dateTopY: Int = Int.MAX_VALUE
        run {
            for (b in blocks) {
                val found = extractDate(b.text)
                if (found != null) {
                    dateIso = found
                    dateTopY = b.boundingBox?.top ?: Int.MAX_VALUE
                    break
                }
            }
        }

        if (dateIso == null) {
            return FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = "日付が見つかりませんでした"
            )
        }


        // --- 4) テキストを二分：基準Yより上(左10%除外), 下(全幅) ---
        val aboveFiltered = StringBuilder()
        val belowAll = StringBuilder()
        for (b in blocks) {
            val bbTop = b.boundingBox?.top ?: Int.MAX_VALUE
            val bbBottom = b.boundingBox?.bottom ?: Int.MAX_VALUE

            if (bbBottom <= dateTopY) {
                // 上側：Element単位で左10%を除外
                for (line in b.lines) {
                    val lineBuf = StringBuilder()
                    for (el in line.elements) {
                        val eb = el.boundingBox
                        val centerX = eb?.let { (it.left + it.right) / 2f } ?: Float.MAX_VALUE
                        /* 呼んだ文字の中心があるx座標より右だったら、lineBufに加える。そうでなければ無視 */
                        if (centerX >= leftExclusionX) {
                            if (lineBuf.isNotEmpty()) lineBuf.append(' ')
                            lineBuf.append(el.text)
                        }
                    }
                    if (lineBuf.isNotEmpty()) {
                        if (aboveFiltered.isNotEmpty()) aboveFiltered.append('\n')
                        aboveFiltered.append(lineBuf.toString())
                    }
                }
            } else {
                // 下側：そのまま
                if (belowAll.isNotEmpty()) belowAll.append('\n')
                belowAll.append(b.text)
            }
        }

        /* 金額抽出 金額は日付の下にあるので、belowAllから探せば良い */
        val amount = extractAmount(belowAll.toString())/* 一番最初に見つけたやつが変えてくる？ */

        /* 日付より上の部分でstoreNameを探す */
        val storeName = extractStoreName(aboveFiltered.toString())

        /**
         * 氏顎にexpenseに入れていく
         */
        result.datetime = dateIso/* nullの可能性もある */
        result.amount = amount
        result.storeName = storeName

        Log.d("PayPayReceiptParser", "dateISo:${dateIso} amount:${amount} storeName: $storeName")

        if (result.amount == null || result.storeName == null) {
            return FuncResultWithData.Warning(
                data = result,
                warningMessage = "取得できない項目がありました"
            )
        }

        return FuncResultWithData.Success(
            data = result
        )
    }

    fun extractDate(text: String): String? {
        val processedT =
            text.replace("B時", "時").replace("\\s+".toRegex(), "")/* なぜかB時になってしまうパターンがあった */

        val dateJaRegex = """(\d{4}年\d{1,2}月\d{1,2}日\s*\d{1,2}時\d{1,2}分)""".toRegex()
        val dateEnRegex = """\d{4}/\d{1,2}/\d{1,2}\s*\d{1,2}:\d{2}""".toRegex()

        dateJaRegex.find(processedT)?.let { jaDate ->
            /* yyyy年mm月dd日 HH時MM分をLocalDateTimeに変換して、それをUTC文字列に変える */
            val formatter = DateTimeFormatter.ofPattern("yyyy年M月d日H時m分")
            val localDateTime = LocalDateTime.parse(jaDate.value, formatter)
            val isoStr = AppTimeZone.localDateTimeToIsoString(localDateTime)

            return isoStr
        }

        dateEnRegex.find(processedT)?.let { enDate ->
            /**
             * 2025/8/1514:56こういう文字列はparseできないから、
             * 意図的に間にスペースをいれる
             */
            val normalized = enDate.value.replace(Regex("(\\d{1,2}):(\\d{2})"), " $1:$2")
            val formatter = DateTimeFormatter.ofPattern("yyyy/M/d/ H:m")
            val localDateTime = LocalDateTime.parse(normalized, formatter)
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

    fun extractStoreName(text: String): String? {
        /**
         * ダイソー\nフジ東予店
         * のパターンと
         * ハローズ\nハローズ 東予店
         * という感じで2行目に店舗名しか入らないパターンと店名も含むパターンがある
         */
        val textSplitted = text.split("\n")
        val name = textSplitted.getOrNull(0)
        val storeName = textSplitted.getOrNull(1)

        if (name == null && storeName == null) {
            return null
        } else if (name == null) {
            return storeName
        } else if (storeName == null) {
            return name
        } else {
            /* nameもstoreNameも入っている */
        }

        /**
         *  nameもstoreNameも入っているとき、
         *  storeNameにnameが入っていたらそのままnameを返す
         *  */
        if (storeName.contains(name)) {
            return storeName
        }

        /* 空欄は取り除いておく */
        return "${name.replace(" ", "")} ${storeName.replace(" ", "")}"
    }
}