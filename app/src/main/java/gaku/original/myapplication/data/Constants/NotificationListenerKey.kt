package gaku.original.myapplication.data.Constants

object NotificationValidTitles {
    /**
     * パッケージ名と通知のタイトル群を対にする
     */
    val appValidTitlesMap: Map<String, Set<String>> = mapOf(
        AppPackageNames.PAYPAY to setOf("支払い完了"),
        AppPackageNames.THIS_APP to setOf("テスト通知")
    )
}