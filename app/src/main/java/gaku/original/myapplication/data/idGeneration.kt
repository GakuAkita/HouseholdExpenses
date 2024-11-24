package gaku.original.myapplication.data

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface idGeneration {
    //idを生成
    //yyyymmddMMHHSS-(番号:同時に生成されてしまったとき)
    //これはここに入れてよいのかな？わからないな。
    fun generateExpenseId(num:Int=0):String{
        val currentDateTime= LocalDateTime.now()
        val datetimeFormat= DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
        val datetimeStr=currentDateTime.format(datetimeFormat)
        val id=datetimeStr+"-"+"${num}"
        return id
    }
}