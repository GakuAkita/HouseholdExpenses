package gaku.original.myapplication

import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.RepeatAdd
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen

@Serializable
data object Splash : Screen

@Serializable
data object AuthGraph {
    @Serializable
    data object Start : Screen

    @Serializable
    data object SignIn : Screen

    @Serializable
    data object ForgotPassword : Screen

    @Serializable
    data object SignUp : Screen
}

@Serializable
data object MainGraph {

    @Serializable
    data object Bottom : Screen {
        @Serializable
        data object Home : Screen

        @Serializable
        data object Search : Screen

        @Serializable
        data object Statistics : Screen

        @Serializable
        data object Setting : Screen
    }

    sealed interface SettingMenu : Screen {
        @Serializable
        data object UserInfo : SettingMenu

        @Serializable
        data object TimeZone : SettingMenu

        @Serializable
        data object Categories : SettingMenu

        sealed interface IRepeatAdd : SettingMenu {
            @Serializable
            data object Screen : IRepeatAdd

            @Serializable
            data class Dialog(val repeatAdd: RepeatAdd? = null) :
                IRepeatAdd
        }

        @Serializable
        data object AmazonSubscribeItem : SettingMenu

        @Serializable
        data object AppSettings : SettingMenu

        @Serializable
        data object AppVersion : SettingMenu

        @Serializable
        data object MailboxExtraction : SettingMenu

        @Serializable
        data object NotificationListenerSetting : SettingMenu

        @Serializable
        data object PayPayReceiptOCRSetting : SettingMenu
    }

    sealed interface Global : Screen {
        @Serializable
        data class ExpenseAddEdit(val expense: Expense? = null) : Global

        @Serializable
        data object CategoryAddEdit : Global

        @Serializable
        data object CategoryAssignment : Global
    }
}

@Serializable
data object SharedReceiverGraph {
    sealed interface SharedReceiver : Screen {
        @Serializable
        data class Entry(val data: SharedData) : SharedReceiver

        @Serializable
        data class PayPayReceiptMaskRatioAdjust(val data: SharedData) : SharedReceiver
    }
}

//sealed class Screen(val route: String) {
//
//    // Start関連
//    sealed class StartScreen(route: String) : Screen(route) {
//        companion object {
//            const val BASE_ROUTE = "start"
//        }
//
//        object Start : StartScreen("$BASE_ROUTE/start")
//        object SignUp : StartScreen("$BASE_ROUTE/sign_up")
//        object Login : StartScreen("$BASE_ROUTE/login")
//        object ForgotPassword : StartScreen("$BASE_ROUTE/forgot_password")
//    }
//
//    // Main関連
//    sealed class MainScreen(route: String) : Screen(route) {
//        companion object {
//            const val BASE_ROUTE = "main"
//        }
//
//        object Content : MainScreen("$BASE_ROUTE/content")
//    }
//
//    //Not Categorized画面
//    object SearchScreen : Screen("search_screen")
//
//    // Graph関連
//    object GraphScreen : Screen("graph_screen")
//
//    //Setting
//    sealed class SettingScreen(route: String) : Screen(route) {
//        companion object {
//            const val BASE_ROUTE = "setting"
//        }
//
//        object Main : SettingScreen("$BASE_ROUTE/main")
//        object UserInfo : SettingScreen("$BASE_ROUTE/user_info")
//        object RepeatAdd : SettingScreen("$BASE_ROUTE/repeat_add")
//        object AppSettings : SettingScreen("$BASE_ROUTE/app_settings")
//        object Version : SettingScreen("$BASE_ROUTE/version")
//
//        sealed class MailboxExtraction(route: String) : SettingScreen(route) {
//            companion object {
//                const val BASE_ROUTE = "${SettingScreen.BASE_ROUTE}/mailbox_extraction"
//            }
//
//            object Main : MailboxExtraction("$BASE_ROUTE/main")
//        }
//
//        object PayPayReceiptOCRSetting :
//            SettingScreen("$BASE_ROUTE/pay_pay_receipt_ocr_setting")
//
//        object NotificationListenerSetting :
//            SettingScreen("$BASE_ROUTE/notification_listener_setting")
//
//        object AmazonSubscribeItems : SettingScreen("$BASE_ROUTE/amazon_subscribe_items")
//    }
//
//    //グローバルな画面
//    // グローバル画面
//    sealed class GlobalScreen(route: String) : MainScreen(route) {
//        companion object {
//            const val EXPENSE_ADD_EDIT_BASE = "expense_add_edit"
//            const val CATEGORY_ADD_EDIT_BASE = "category_add_edit"
//            const val CATEGORY_ASSIGNMENT_EDIT_BASE = "category_assignment_edit"
//            const val OCR_BASE = "ocr"
//            const val NLS_BASE = "notification_listener_process"
//        }
//
//        object ExpenseAddEdit : GlobalScreen("$EXPENSE_ADD_EDIT_BASE?from={from}") {
//            fun createRoute(from: Screen) = "$EXPENSE_ADD_EDIT_BASE?from=${from.route}"
//        }
//
//        object CategoryAddEdit : GlobalScreen(CATEGORY_ADD_EDIT_BASE)
//
//        object CategoryAssignmentEdit : GlobalScreen(CATEGORY_ASSIGNMENT_EDIT_BASE)
//
//        // ---OCR 関連をまとめた -----
//        sealed class OCR(route: String) : GlobalScreen(route) {
//            companion object {
//                const val BASE_ROUTE = GlobalScreen.OCR_BASE
//            }
//
//            object Entry : OCR("$BASE_ROUTE/entry")
//            object Read : OCR("$BASE_ROUTE/read")
//            object MaskRatioAdjust : OCR("$BASE_ROUTE/mask_ratio_adjust")
//        }
//
//        object NotificationListenerProcess : GlobalScreen(NLS_BASE)
//    }
//}