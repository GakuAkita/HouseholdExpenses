package gaku.original.myapplication.data.Constants

object NotificationValidTitles {
    val appValidTitlesMap: Map<String, Set<String>> = mapOf(
        AppPackageNames.PAYPAY to setOf("支払い完了"),
        AppPackageNames.THIS_APP to setOf("テスト通知")
    )
}