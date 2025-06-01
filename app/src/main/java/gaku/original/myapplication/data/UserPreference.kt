package gaku.original.myapplication.data

data class UserPreferences(
    var timeZone: String = "Asia/Tokyo" // Default time zone
    //    var isDarkMode: Boolean = false
//    var isNotificationEnabled: Boolean = true
) {
    companion object {
        const val FIELD_TIME_ZONE = "timeZone"//必ず上のキー名と一致させる
        // const val FIELD_IS_DARK_MODE = "isDarkMode"
        // const val FIELD_IS_NOTIFICATION_ENABLED = "isNotificationEnabled"
    }
}