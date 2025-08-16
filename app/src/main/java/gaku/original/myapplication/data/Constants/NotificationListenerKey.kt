package gaku.original.myapplication.data.Constants

object NotificationListenerKey {
    const val PACKAGE_NAME = "package_name"
    const val TITLE = "title"
    const val TEXT = "text"
    const val UNIX_MILLIS = "unix_millis"
}

object NotificationValidTitles {
    val appValidTitlesMap: Map<String, Set<String>> = mapOf(
        AppPackageNames.PAYPAY to setOf("支払い完了"),
        AppPackageNames.THIS_APP to setOf("テスト通知")
    )
}