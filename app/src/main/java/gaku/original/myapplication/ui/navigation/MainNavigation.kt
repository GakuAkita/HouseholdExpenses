package gaku.original.myapplication.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import gaku.original.myapplication.MainGraph

fun NavGraphBuilder.mainGraph(
    navController: NavHostController
){
    navigation<MainGraph>(
        startDestination = MainGraph
    ){
        composable<MainGraph.Bottom> {
            /* MainFrame */
            /* There is another navHostController. */
        }

        composable<MainGraph.Global.ExpenseAddEdit> {

        }

        composable<MainGraph.Global.CategoryAddEdit> {

        }

        composable<MainGraph.Global.CategoryAssignmentEdit> {

        }
    }
}