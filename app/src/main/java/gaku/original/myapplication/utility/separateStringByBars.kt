package gaku.original.myapplication.utility

fun separateStringByBars(str: String): List<String> {
    return str.split("___")
}

fun concatStringWithBars(strList: List<String>): String {
    val ret = strList.joinToString("___")
    return ret
}