package gaku.original.myapplication.data.dataClass

import gaku.original.myapplication.data.Constants.TimeZoneOption

data class UserPreferences(
    var timeZone: String = TimeZoneOption.JAPAN.id // Default time zone
    //    var isDarkMode: Boolean = false
//    var isNotificationEnabled: Boolean = true
) {
    companion object {
        const val FIELD_TIME_ZONE = "timeZone"//必ず上のキー名と一致させる
        // const val FIELD_IS_DARK_MODE = "isDarkMode"
        // const val FIELD_IS_NOTIFICATION_ENABLED = "isNotificationEnabled"
    }
}

fun UserPreferences.toMap(): Map<String, Any> {
    return mapOf(
        UserPreferences.FIELD_TIME_ZONE to timeZone
        // UserPreferences.FIELD_IS_DARK_MODE to isDarkMode,
        // UserPreferences.FIELD_IS_NOTIFICATION_ENABLED to isNotificationEnabled
    )
}

fun getDefaultUserPreferences(): UserPreferences {
    return UserPreferences(
        timeZone = TimeZoneOption.JAPAN.id
        // isDarkMode = false,
        // isNotificationEnabled = true
    )
}