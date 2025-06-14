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
        object ExpenseAddEdit : MainScreen("$BASE_ROUTE/expense_add_edit")
        object CategoryAddEdit : MainScreen("$BASE_ROUTE/category_add_edit")
    }

    // Graph関連
    object GraphScreen : Screen("graph_screen")

    //Setting
    sealed class SettingScreen(route: String) : Screen(route) {
        companion object {
            const val BASE_ROUTE = "setting"
        }

        object Main : MainScreen("$BASE_ROUTE/main")
        object UserInfo : MainScreen("$BASE_ROUTE/user_info")
        object RepeatAdd : MainScreen("$BASE_ROUTE/repeat_add")
        object AppSettings : MainScreen("$BASE_ROUTE/app_settings")

        sealed class MailAutoExtraction(route: String) : SettingScreen(route) {
            companion object {
                const val BASE_ROUTE = "${SettingScreen.BASE_ROUTE}/mail_auto_extraction"
            }

            object Main : MailAutoExtraction("$BASE_ROUTE/main")
            object RakutenPay : MailAutoExtraction("$BASE_ROUTE/rakuten_pay")
            object AmazonKindle : MailAutoExtraction("$BASE_ROUTE/amazon_kindle")
            object AmazonItem : MailAutoExtraction("$BASE_ROUTE/amazon_item")
            object ShikokuElectricPower : MailAutoExtraction("$BASE_ROUTE/shikoku_electric_power")
        }
    }

    //使わない
    object NotCategorizedScreen : Screen("not_categorized_screen")
}