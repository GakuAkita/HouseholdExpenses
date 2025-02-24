package gaku.original.myapplication

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import gaku.original.myapplication.ui.view.GraphView
import gaku.original.myapplication.ui.view.NotCategorizedView
import gaku.original.myapplication.ui.view.main.CategoryAddEditView
import gaku.original.myapplication.ui.view.main.ExpenseAddEditView
import gaku.original.myapplication.ui.view.main.MainView
import gaku.original.myapplication.ui.view.settings.RepeatAddSettingView
import gaku.original.myapplication.ui.view.settings.SettingsView
import gaku.original.myapplication.ui.view.settings.UserInfoView
import gaku.original.myapplication.ui.view.start.LoginSignUpView
import gaku.original.myapplication.ui.view.start.StartView
import gaku.original.myapplication.viewModel.AuthManagerViewModel


@Composable
fun Navigation(
) {
    val navController = rememberNavController()

    //サインインすでにしているかをみたい
    val authManagerViewModel: AuthManagerViewModel = hiltViewModel()

    /* こうすることで、再起動前にログインしていた場合、MainViewに直接飛ぶ */
    val startDestination: String
    if (authManagerViewModel.isSignedIn) {
        startDestination = Screen.MainScreen.Content.route
    } else {
        startDestination = Screen.StartScreen.Start.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        //Startスクリーン
        composable(Screen.StartScreen.Start.route) {
            StartView(navController = navController)
        }
        composable(Screen.StartScreen.SignUp.route) {
            val isLogin = false
            LoginSignUpView(navController = navController, isLogin = isLogin)
        }
        composable(Screen.StartScreen.Login.route) {
            val isLogin = true
            LoginSignUpView(navController = navController, isLogin = isLogin)
        }

        //Mainスクリーン
        composable(Screen.MainScreen.Content.route)
        {
            MainView(navController = navController)
        }

        composable(Screen.MainScreen.ExpenseAddEdit.route) {
            ExpenseAddEditView(navController = navController)
        }
        composable(Screen.MainScreen.CategoryAddEdit.route) {
            CategoryAddEditView(navController = navController)
        }

        //Graphスクリーン
        composable(Screen.GraphScreen.route) {
            GraphView(navController)
        }

        //NotCategorizedスクリーン
        composable(Screen.NotCategorizedScreen.route) {
            NotCategorizedView(navController)
        }

        //Settingsスクリーン
        composable(Screen.SettingScreen.Main.route) {
            SettingsView(navController = navController)
        }
        composable(Screen.SettingScreen.UserInfo.route) {
            UserInfoView(navController = navController)
        }
        composable(Screen.SettingScreen.RepeatAdd.route) {
            RepeatAddSettingView(navController = navController)
        }
    }
}