package gaku.original.myapplication.parser

import android.util.Log
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.Text.TextBlock
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.FuncResultWithData
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.getDefaultExpense
import gaku.original.myapplication.data.repository.appTimeZone.toIsoUtcString
import gaku.original.myapplication.utility.LogAkitaDebug
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class PayPayReceiptOCRParser(
    val textOcr: Text?,
) {
    val className = PayPayReceiptOCRParser::class.java.simpleName
    fun parse(
        text: Text? = textOcr,
        exclusionRatioFromScreenLeft: Float = 0.22f,
        imageWidth: Int = 0,
    ): FuncResultWithData<Expense> {
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
//        val minX = 0
//        val maxX = imageWidth
//        if (maxX == Int.MIN_VALUE) {
//            return FuncResultWithData.Failure.GenericFailure(
//                status = FuncStatus.FAILED,
//                errorMessage = "OCR:画面幅を取得できませんでした"
//            )
//        }
//        val ratioFromLeft = exclusionRatioFromScreenLeft/* ここは携帯の画面によるのか。ユーザーに調整してもらうしかないな？？ */
//        val leftExclusionX = minX + ((maxX - minX) * ratioFromLeft) // 左数十%
//        Log.d(
//            className,
//            "minX:$minX maxX:$maxX ratioFromLeft:$ratioFromLeft leftExclusionX:$leftExclusionX"
//        )
//        LogAkitaDebug("leftExclusionX=${leftExclusionX}")

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

        for (b in blocks) {
            val found = extractDate(b.text)
            if (found != null) {
                dateIso = found
                dateTopY = b.boundingBox?.top ?: Int.MAX_VALUE
                break
            }
        }

        if (dateIso == null) {
            return FuncResultWithData.Failure.GenericFailure(
                status = FuncStatus.FAILED,
                errorMessage = "日付が見つかりませんでした"
            )
        }

        /* 日付より上のblockと下のblockに分ける */
        val aboveDateBlocks = mutableListOf<TextBlock>()
        val belowAll = StringBuilder()
        for (b in blocks) {
            val bbBottom = b.boundingBox?.bottom ?: Int.MAX_VALUE
            if (bbBottom <= dateTopY) {
                aboveDateBlocks.add(b)
            } else {
                // 下側：そのまま
                if (belowAll.isNotEmpty()) belowAll.append('\n')
                belowAll.append(b.text)
            }
        }
        LogAkitaDebug("belowAll=${belowAll}")

        LogAkitaDebug("blockの個数 = ${aboveDateBlocks.size}")


        val aboveFiltered = StringBuilder()/* dateより上の文字列(店名)が格納されていく */
        /**
         * こんなに面倒なことはやらなくてよいが、、
         * 読み込む時点でロゴはマスクしているので、、
         * 将来的に各文字の位置情報を取りたいとき用に残しておく
         */
        for (b in aboveDateBlocks) {
            for (line in b.lines) {
                val lineBuf = StringBuilder()/* ブロック内のlineの一行分を読む。 */
                LogAkitaDebug("This is line--------------")
                for (el in line.elements) {
                    LogAkitaDebug("This is element--------------")
                    for (symbol in el.symbols) {
                        val eb = symbol.boundingBox
                        LogAkitaDebug("This is debug.. symbol:${symbol.text} left=${eb?.left} right=${eb?.right}")
                        lineBuf.append(symbol.text)
                    }
                }
                aboveFiltered.append(lineBuf.toString() + "\n")
            }
        }
        LogAkitaDebug(aboveFiltered.toString())

        /* 金額抽出 金額は日付の下にあるので、belowAllから探せば良い */
        val amount = extractAmount(belowAll.toString())/* 一番最初に見つけたやつが返ってくる？ */

        /* 日付より上の部分でstoreNameを探す */
        val storeName = extractStoreName(aboveFiltered.toString())

        /**
         * 氏顎にexpenseに入れていく
         */
        result.datetime = dateIso/* nullの可能性もある */
        result.amount = amount
        result.storeName = storeName

        Log.d(className, "dateISo:${dateIso} amount:${amount} storeName: $storeName")

        if (result.amount == null || result.storeName == null) {
            return FuncResultWithData.Warning(
                data = result,
                warningMessage = "取得できない項目がありました。\n費用:${result.amount} 店名:[${result.storeName}]"
            )
        }

        return FuncResultWithData.Success(
            data = result
        )
    }

    fun extractDate(text: String): String? {
        // OCRのノイズ対策：B時を時に置換、全角スペースや複数スペースを削除
        val processedT = text.replace("B時", "時").replace("\\s+".toRegex(), " ").trim()

        // 日本語形式：yyyy年M月d日H時m分（スペース有無対応）
        val dateJaRegex = """(\d{4}年\d{1,2}月\d{1,2}日\s*\d{1,2}時\d{1,2}分)""".toRegex()
        dateJaRegex.find(processedT)?.value?.let { jaDate ->
            val normalized = jaDate.replace("\\s+".toRegex(), "") // スペース削除
            val formatter = DateTimeFormatter.ofPattern("yyyy年M月d日H時m分")
            val localDateTime = LocalDateTime.parse(normalized, formatter)
            return TODO()
        }

        // 英数字形式：yyyy/M/d H:m（スペース有無や時刻くっつき対応）
        val dateEnRegex = """(\d{4}/\d{1,2}/\d{1,2}\s*\d{1,2}:\d{2})""".toRegex()
        dateEnRegex.find(processedT)?.value?.let { enDate ->
            // 時刻の前に必ずスペースを入れる
            val normalized = enDate.replace(Regex("(\\d{1,2}):(\\d{2})"), " $1:$2")
                .replace("\\s+".toRegex(), " ") // 複数スペースを1つに
                .trim()
            val formatter = DateTimeFormatter.ofPattern("yyyy/M/d H:m")
            val localDateTime = LocalDateTime.parse(normalized, formatter)
            return TODO()
        }

        // どちらにもマッチしなかった場合
        return null
    }


    fun extractAmount(text: String): Long? {
        /* 日本語 */
        val amountJaRegex = """(\d{1,3}(,\d{3})*円)""".toRegex()

        /* 英語 */
        val amountEnRegex = """\d{1,3}(,\d{3})*\s*yen""".toRegex()

        amountJaRegex.find(text)?.let {
            return it.value.replace(",", "").replace("円", "").trim().toLongOrNull()
        }

        amountEnRegex.find(text)?.let {
            return it.value.replace(",", "").replace("yen", "").trim().toLongOrNull()
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
        if (text == "") {
            return null
        }
        val textSplit = text.split("\n")
        val name = textSplit.getOrNull(0)
        val storeName = textSplit.getOrNull(1)

        Log.d(className, "name: $name storeName: $storeName")

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