package gaku.original.myapplication.utility

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// File: Extensions.kt
fun <T : Any> T.toMap(clazz: Class<T>): Map<String, Any> {
    val gson = Gson()
    val json = gson.toJson(this)
    val type = object : TypeToken<Map<String, Any>>() {}.type
    return gson.fromJson(json, type)
}