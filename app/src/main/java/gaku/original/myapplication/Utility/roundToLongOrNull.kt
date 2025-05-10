package gaku.original.myapplication.Utility

import android.util.Log
import kotlin.math.roundToLong

fun Double.roundToLongOrNull(): Long? {
    return try {
        if (this in Long.MIN_VALUE.toDouble()..Long.MAX_VALUE.toDouble()) {
            this.roundToLong()
        } else {
            Log.d("roundToLongOrNull", "Value out of range for Long: $this")
            null
        }
    } catch (e: Exception) {
        null
    }
}