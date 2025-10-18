package gaku.original.myapplication.utility

import android.util.Log

fun LogTimeout(tag: String, funcname: String, e: Exception) {
    val msg = "${funcname} Tiimeout : ${e.message}"
    Log.d(tag, msg)
}