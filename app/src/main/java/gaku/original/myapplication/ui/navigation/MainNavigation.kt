package gaku.original.myapplication.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import gaku.original.myapplication.MainGraph
import gaku.original.myapplication.ui.screens.bottom.MainFrame
import gaku.original.myapplication.ui.screens.global.expenseAddEdit.ExpenseAddEditScreenRoot

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

        composable<MainGraph.Global.ExpenseAddEdit> {
            ExpenseAddEditScreenRoot(
                navHostController = navController
            )
        }

        composable<MainGraph.Global.CategoryAddEdit> {

        }

        composable<MainGraph.Global.CategoryAssignmentEdit> {

        }
    }
}