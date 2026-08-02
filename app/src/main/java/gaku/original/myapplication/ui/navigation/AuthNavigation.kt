package gaku.original.myapplication.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import gaku.original.myapplication.AuthGraph

fun NavGraphBuilder.authGraph(navController: NavHostController) {
    navigation<AuthGraph>(
        startDestination = AuthGraph.Start
    ){
        composable<AuthGraph.Start>{

        }

        composable<AuthGraph.SignIn>{

        }

        composable<AuthGraph.SignUp>{

        }
    }
}