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

    // その他
    object GraphScreen : Screen("graph_screen")
    object NotCategorizedScreen : Screen("not_categorized_screen")
    object SettingScreen : Screen("setting_screen")
}