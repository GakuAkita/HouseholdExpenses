package gaku.original.myapplication.Utility

import android.util.Log

fun LogClassFuncCalled(className: String, funcName: String) {
    Log.d(className, "${funcName} was called.")
}