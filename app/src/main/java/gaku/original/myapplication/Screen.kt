package gaku.original.myapplication

import gaku.original.myapplication.data.dataClass.CategoryAssignment
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
            data class EditDialog(val repeatAdd: RepeatAdd? = null) :
                IRepeatAdd

            @Serializable
            data class ExecuteDialog(val repeatAdd: RepeatAdd)
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

        sealed interface ICategoryAssignment : Global {
            @Serializable
            data object Screen : ICategoryAssignment

            @Serializable
            data class EditDialog(
                val assignment: CategoryAssignment?
            ) : ICategoryAssignment {
                companion object {
                    const val KEY_UPDATED = "assignment_updated"
                }
            }
        }
    }
}

@Serializable
data object SharedReceiverGraph {
    sealed interface SharedReceiver : Screen {
        @Serializable
        data class Entry(val data: SharedData) : SharedReceiver

        @Serializable
        data class PayPayReceiptMaskRatioAdjust(val imagePath: String) : SharedReceiver
    }
}