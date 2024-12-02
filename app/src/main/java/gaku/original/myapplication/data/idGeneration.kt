package gaku.original.myapplication.data

import gaku.original.myapplication.fromLocalDateTime
import java.time.LocalDateTime

interface idGeneration {
    //idを生成
    //yyyymmddMMHHSS-(番号:同時に生成されてしまったとき)
    //これはここに入れてよいのかな？わからないな。
    fun generateExpenseId(num:Int=0):String{
        val currentDateTime= LocalDateTime.now()
        val datetimeStr= fromLocalDateTime(currentDateTime)
        val id=datetimeStr+"-"+"${num}"
        return id
    }
}