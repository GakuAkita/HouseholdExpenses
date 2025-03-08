package gaku.original.myapplication.Utility

import android.util.Log

fun LogUnexpectedError(tag: String, funcName: String, e: Exception) {
    val msg = "Unexpected Error occurred in ${funcName} : ${e.message}"
    Log.d(tag, msg)
}