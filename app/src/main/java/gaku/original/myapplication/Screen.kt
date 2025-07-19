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
    object NotCategorizedScreen : Screen("not_categorized_screen")

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

        sealed class GmailMailboxExtraction(route: String) : SettingScreen(route) {
            companion object {
                const val BASE_ROUTE = "${SettingScreen.BASE_ROUTE}/gmail_mailbox_extraction"
            }

            object Main : GmailMailboxExtraction("$BASE_ROUTE/main")

            //こいつら使わないかも
            object RakutenPay : GmailMailboxExtraction("$BASE_ROUTE/rakuten_pay")
            object AmazonKindle : GmailMailboxExtraction("$BASE_ROUTE/amazon_kindle")
            object AmazonItem : GmailMailboxExtraction("$BASE_ROUTE/amazon_item")
            object ShikokuElectricPower :
                GmailMailboxExtraction("$BASE_ROUTE/shikoku_electric_power")
        }
    }

    //グローバルな画面
    // グローバル画面
    sealed class GlobalScreen(route: String) : MainScreen(route) {
        companion object {
            const val EXPENSE_ADD_EDIT_BASE = "expense_add_edit"
            const val CATEGORY_ADD_EDIT_BASE = "category_add_edit"
        }

        object ExpenseAddEdit : GlobalScreen("$EXPENSE_ADD_EDIT_BASE?from={from}") {
            fun createRoute(from: Screen) = "$EXPENSE_ADD_EDIT_BASE?from=${from.route}"
        }

        object CategoryAddEdit : GlobalScreen(CATEGORY_ADD_EDIT_BASE)
    }
}