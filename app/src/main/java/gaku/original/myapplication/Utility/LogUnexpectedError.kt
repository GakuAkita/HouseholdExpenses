package gaku.original.myapplication.Utility

import android.util.Log

fun LogException(tag: String, funcName: String, e: Exception) {
    val msg = "Error occurred in ${funcName} : ${e.message}"
    Log.d(tag, msg)
}