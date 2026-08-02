package gaku.original.myapplication.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import gaku.original.myapplication.AuthGraph
import gaku.original.myapplication.ui.screens.start.StartView
import gaku.original.myapplication.ui.screens.start.signin.SignInScreenRoot

fun NavGraphBuilder.authGraph(navController: NavHostController) {
    navigation<AuthGraph>(
        startDestination = AuthGraph.Start
    ){
        composable<AuthGraph.Start>{
            StartView(
                onSignInClick = {
                    navController.navigate(AuthGraph.SignIn)
                },
                onSignUpClick = {
                    navController.navigate(AuthGraph.SignUp)
                }
            )
        }

        composable<AuthGraph.SignIn>{
            SignInScreenRoot(
                isSignIn = true,
                isGoogleOnly = true
            )
        }

        composable<AuthGraph.SignUp>{
            SignInScreenRoot(
                isSignIn = false,
                isGoogleOnly = true
            )
        }

        composable<AuthGraph.ForgotPassword> {

        }
    }
}