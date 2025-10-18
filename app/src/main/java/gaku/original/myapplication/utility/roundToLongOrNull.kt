package gaku.original.myapplication.utility

import android.util.Log
import kotlin.math.roundToLong

fun String.roundToLongOrNull(): Long? {
    return try {
        val value = this.toDouble()
        if (value in Long.MIN_VALUE.toDouble()..Long.MAX_VALUE.toDouble()) {
            value.roundToLong()
        } else {
            Log.d("roundToLongOrNull", "Value out of range for Long: $value")
            null
        }
    } catch (e: Exception) {
        null
    }
}