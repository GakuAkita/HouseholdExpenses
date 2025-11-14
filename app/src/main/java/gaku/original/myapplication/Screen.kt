package gaku.original.myapplication

sealed class Screen(val route: String) {

    // Start関連
    sealed class StartScreen(route: String) : Screen(route) {
        companion object {
            const val BASE_ROUTE = "start"
        }

        object Start : StartScreen("$BASE_ROUTE/start")
        object SignUp : StartScreen("$BASE_ROUTE/sign_up")
        object Login : StartScreen("$BASE_ROUTE/login")
        object ForgotPassword : StartScreen("$BASE_ROUTE/forgot_password")
    }

    // Main関連
    sealed class MainScreen(route: String) : Screen(route) {
        companion object {
            const val BASE_ROUTE = "main"
        }

        object Content : MainScreen("$BASE_ROUTE/content")
    }

    //Not Categorized画面
    object SearchScreen : Screen("search_screen")

    // Graph関連
    object GraphScreen : Screen("graph_screen")

    //Setting
    sealed class SettingScreen(route: String) : Screen(route) {
        companion object {
            const val BASE_ROUTE = "setting"
        }

        object Main : SettingScreen("$BASE_ROUTE/main")
        object UserInfo : SettingScreen("$BASE_ROUTE/user_info")
        object RepeatAdd : SettingScreen("$BASE_ROUTE/repeat_add")
        object AppSettings : SettingScreen("$BASE_ROUTE/app_settings")
        object Version : SettingScreen("$BASE_ROUTE/version")

        sealed class MailboxExtraction(route: String) : SettingScreen(route) {
            companion object {
                const val BASE_ROUTE = "${SettingScreen.BASE_ROUTE}/mailbox_extraction"
            }

            object Main : MailboxExtraction("$BASE_ROUTE/main")
        }

        object PayPayReceiptOCRSetting :
            SettingScreen("$BASE_ROUTE/pay_pay_receipt_ocr_setting")

        object NotificationListenerSetting :
            SettingScreen("$BASE_ROUTE/notification_listener_setting")

        object AmazonSubscribeItems : SettingScreen("$BASE_ROUTE/amazon_subscribe_items")
    }

    //グローバルな画面
    // グローバル画面
    sealed class GlobalScreen(route: String) : MainScreen(route) {
        companion object {
            const val EXPENSE_ADD_EDIT_BASE = "expense_add_edit"
            const val CATEGORY_ADD_EDIT_BASE = "category_add_edit"
            const val CATEGORY_ASSIGNMENT_EDIT_BASE = "category_assignment_edit"
            const val OCR_BASE = "ocr"
            const val NLS_BASE = "notification_listener_process"
        }

        object ExpenseAddEdit : GlobalScreen("$EXPENSE_ADD_EDIT_BASE?from={from}") {
            fun createRoute(from: Screen) = "$EXPENSE_ADD_EDIT_BASE?from=${from.route}"
        }

        object CategoryAddEdit : GlobalScreen(CATEGORY_ADD_EDIT_BASE)

        object CategoryAssignmentEdit : GlobalScreen(CATEGORY_ASSIGNMENT_EDIT_BASE)

        // ---OCR 関連をまとめた -----
        sealed class OCR(route: String) : GlobalScreen(route) {
            companion object {
                const val BASE_ROUTE = GlobalScreen.OCR_BASE
            }

            object Entry : OCR("$BASE_ROUTE/entry")
            object Read : OCR("$BASE_ROUTE/read")
            object MaskRatioAdjust : OCR("$BASE_ROUTE/mask_ratio_adjust")
        }

        object NotificationListenerProcess : GlobalScreen(NLS_BASE)
    }
}