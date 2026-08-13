package gaku.original.myapplication.ui.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import gaku.original.myapplication.MainGraph
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.ui.screens.bottom.MainFrame
import gaku.original.myapplication.ui.screens.global.categoryEdit.CategoryEditScreenRoot
import gaku.original.myapplication.ui.screens.global.expenseAddEdit.ExpenseAddEditScreenRoot
import gaku.original.myapplication.ui.screens.global.expenseAddEdit.ExpenseAddEditViewModel
import kotlin.reflect.typeOf
import kotlin.to

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

        }

        composable<MainGraph.SettingMenu.TimeZone> {

        }

        composable<MainGraph.SettingMenu.Categories> {

        }

        composable<MainGraph.SettingMenu.RepeatAdd> {

        }

        composable<MainGraph.SettingMenu.MailboxExtraction> {

        }

        composable<MainGraph.SettingMenu.PayPayReceiptOCRSetting> {

        }

        composable<MainGraph.SettingMenu.AppVersion> {

        }
    }
}