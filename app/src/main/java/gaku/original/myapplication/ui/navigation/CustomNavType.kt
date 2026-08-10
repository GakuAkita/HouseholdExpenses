package gaku.original.myapplication.ui.navigation

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Source - https://stackoverflow.com/a/78988054
// Posted by ABADA S, modified by community. See post 'Timeline' for change history
// Retrieved 2026-08-10, License - CC BY-SA 4.0

inline fun <reified T> navTypeOf(
    json: Json = Json,
) = object : NavType<T>(false) {
    override fun get(bundle: Bundle, key: String): T? =
        bundle.getString(key)?.let(json::decodeFromString)

    override fun parseValue(value: String): T = json.decodeFromString(Uri.decode(value))

    override fun serializeAsValue(value: T): String = Uri.encode(json.encodeToString(value))

    override fun put(bundle: Bundle, key: String, value: T) =
        bundle.putString(key, json.encodeToString(value))

}

inline fun <reified T> nullableNavTypeOf(
    json: Json = Json,
) = object : NavType<T?>(true) {
    override fun get(bundle: Bundle, key: String): T? =
        bundle.getString(key)?.let(json::decodeFromString)

    override fun parseValue(value: String): T? =
        if (value == "null") null else json.decodeFromString(Uri.decode(value))

    override fun serializeAsValue(value: T?): String =
        value?.let {
            Uri.encode(Json.encodeToString(it))
        } ?: "null"

    override fun put(bundle: Bundle, key: String, value: T?) {
        bundle.putString(
            key,
            value?.let(json::encodeToString)
        )
    }

}