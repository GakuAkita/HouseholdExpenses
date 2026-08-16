package gaku.original.myapplication.ui.navigation

import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navigation
import androidx.navigation.toRoute
import gaku.original.myapplication.MainGraph
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.RepeatAdd
import gaku.original.myapplication.ui.screens.bottom.MainFrame
import gaku.original.myapplication.ui.screens.global.categoryEdit.CategoryEditScreenRoot
import gaku.original.myapplication.ui.screens.global.expenseAddEdit.ExpenseAddEditScreenRoot
import gaku.original.myapplication.ui.screens.global.expenseAddEdit.ExpenseAddEditViewModel
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.MailboxExtractionScreenRoot
import gaku.original.myapplication.ui.screens.global.settingMenu.repeatAdd.RepeatAddScreenRoot
import gaku.original.myapplication.ui.screens.global.settingMenu.repeatAdd.editDialog.RepeatAddEditDialogRoot
import gaku.original.myapplication.ui.screens.global.settingMenu.repeatAdd.editDialog.RepeatAddEditViewModel
import gaku.original.myapplication.ui.screens.global.settingMenu.timezone.TimeZoneScreenRoot
import gaku.original.myapplication.ui.screens.global.settingMenu.userInfo.UserInfoScreenRoot
import gaku.original.myapplication.ui.screens.global.settingMenu.version.VersionScreen
import kotlin.reflect.typeOf

fun NavGraphBuilder.mainGraph(
    navController: NavHostController
) {
    navigation<MainGraph>(
        startDestination = MainGraph.Bottom
    ) {
        composable<MainGraph.Bottom> {
            /* MainFrame */
            /* There is another navHostController. */
            MainFrame(
                navController
            )
        }

        // https://medium.com/mercadona-tech/type-safety-in-navigation-compose-23c03e3d74a5
        composable<MainGraph.Global.ExpenseAddEdit>(
            typeMap = mapOf(typeOf<Expense?>() to nullableNavTypeOf<Expense>())
        ) { backStackEntry ->
            val expense = backStackEntry.toRoute<MainGraph.Global.ExpenseAddEdit>().expense

            ExpenseAddEditScreenRoot(
                navHostController = navController,
                viewModel = viewModel(factory = ExpenseAddEditViewModel.Factory(expense))
            )
        }

        composable<MainGraph.Global.CategoryAddEdit> {
            CategoryEditScreenRoot(
                navHostController = navController
            )
        }

        composable<MainGraph.Global.CategoryAssignmentEdit> {

        }

        /* -------------- Setting ------------------ */
        composable<MainGraph.SettingMenu.UserInfo> {
            UserInfoScreenRoot(
                navHostController = navController
            )
        }

        composable<MainGraph.SettingMenu.TimeZone> {
            TimeZoneScreenRoot(
                navHostController = navController
            )
        }

        composable<MainGraph.SettingMenu.Categories> {
            /* Same screen, but different route. */
            CategoryEditScreenRoot(
                navHostController = navController
            )
        }

        composable<MainGraph.SettingMenu.IRepeatAdd.Screen> {
            /* RepeatAddViewModel is shared with RepeatAdd.Dialog */
            RepeatAddScreenRoot(
                navHostController = navController
            )
        }

        dialog<MainGraph.SettingMenu.IRepeatAdd.Dialog>(
            typeMap = mapOf(typeOf<RepeatAdd?>() to nullableNavTypeOf<RepeatAdd>()),
            dialogProperties = DialogProperties(
                dismissOnClickOutside = false
            )
        ) { backStackEntry ->
            val repeatAdd =
                backStackEntry.toRoute<MainGraph.SettingMenu.IRepeatAdd.Dialog>().repeatAdd
            RepeatAddEditDialogRoot(
                viewModel = viewModel(factory = RepeatAddEditViewModel.Factory(repeatAdd)),
                navHostController = navController
            )
        }


        composable<MainGraph.SettingMenu.MailboxExtraction> {
            MailboxExtractionScreenRoot(
                navHostController = navController
            )
        }

        composable<MainGraph.SettingMenu.PayPayReceiptOCRSetting> {

        }

        composable<MainGraph.SettingMenu.AppVersion> {
            VersionScreen(navController)
        }
    }
}