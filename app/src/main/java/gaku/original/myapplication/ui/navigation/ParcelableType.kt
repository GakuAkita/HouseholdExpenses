package gaku.original.myapplication.ui.navigation

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.NavType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Source - https://stackoverflow.com/a/78988054
// Posted by ABADA S, modified by community. See post 'Timeline' for change history
// Retrieved 2026-08-10, License - CC BY-SA 4.0

inline fun <reified T> navTypeOf(
    isNullableAllowed: Boolean = false,/* this doesn't seem to be effective. */
    json: Json = Json,
) = object : NavType<T>(isNullableAllowed = isNullableAllowed) {
    override fun get(bundle: Bundle, key: String): T? =
        bundle.getString(key)?.let(json::decodeFromString)

    override fun parseValue(value: String): T = json.decodeFromString(Uri.decode(value))

    override fun serializeAsValue(value: T): String = Uri.encode(json.encodeToString(value))

    override fun put(bundle: Bundle, key: String, value: T) =
        bundle.putString(key, json.encodeToString(value))

}
