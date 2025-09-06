package gaku.original.myapplication.ui.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import gaku.original.myapplication.Screen
import gaku.original.myapplication.ui.view.main.CategoryAddEditView
import gaku.original.myapplication.ui.view.main.ExpenseAddEditView
import gaku.original.myapplication.ui.view.main.GraphView
import gaku.original.myapplication.ui.view.main.MainView
import gaku.original.myapplication.ui.view.main.SearchView
import gaku.original.myapplication.ui.view.settings.SettingsView
import gaku.original.myapplication.ui.view.settings.menu.AppSettingsView
import gaku.original.myapplication.ui.view.settings.menu.CategoryAssignmentEditView
import gaku.original.myapplication.ui.view.settings.menu.MailboxExtractionView
import gaku.original.myapplication.ui.view.settings.menu.NotificationListenerSettingView
import gaku.original.myapplication.ui.view.settings.menu.RepeatAddSettingView
import gaku.original.myapplication.ui.view.settings.menu.UserInfoView
import gaku.original.myapplication.ui.view.start.ForgotPasswordView
import gaku.original.myapplication.ui.view.start.LoginSignUpView
import gaku.original.myapplication.ui.view.start.StartView


@Composable
fun Navigation(
    navController: NavHostController,
    startDestination: String = Screen.StartScreen.Start.route
) {
    NavHost(
        modifier = Modifier.fillMaxSize()/* これをつけるとnavigateすると左上から右下に行くやつがなくなる？　*/,
        navController = navController,
        startDestination = startDestination
    ) {

        //Startスクリーン
        composable(Screen.StartScreen.Start.route) {
            StartView(navController = navController)
        }
        composable(Screen.StartScreen.SignUp.route) {
            val isLogin = false
            LoginSignUpView(
                navController = navController,
                isLogin = isLogin
            )
        }
        composable(Screen.StartScreen.Login.route) {
            val isLogin = true
            LoginSignUpView(
                navController = navController,
                isLogin = isLogin,
                googleOnly = true
            )
        }
        composable(Screen.StartScreen.ForgotPassword.route) {
            ForgotPasswordView(navController = navController)
        }

        //Mainスクリーン
        composable(Screen.MainScreen.Content.route)
        {
            MainView(navController = navController)
        }

        composable(route = Screen.GlobalScreen.ExpenseAddEdit.route, arguments = listOf(
            navArgument("from") {
                type = NavType.StringType
                defaultValue = "unknown"
            }
        )) { backStackEntry ->
            val from = backStackEntry.arguments?.getString("from") ?: "unknown"
            ExpenseAddEditView(navController = navController, from = from)
        }
        composable(Screen.GlobalScreen.CategoryAddEdit.route) {
            CategoryAddEditView(navController = navController)
        }

        //Graphスクリーン
        composable(Screen.GraphScreen.route) {
            GraphView(navController = navController)
        }

        //NotCategorizedスクリーン
        composable(Screen.SearchScreen.route) {
            SearchView(navController = navController)
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
        composable(Screen.SettingScreen.AppSettings.route) {
            AppSettingsView(navController = navController)
        }
        composable(Screen.SettingScreen.MailboxExtraction.Main.route) {
            MailboxExtractionView(navController = navController)
        }
        composable(Screen.GlobalScreen.CategoryAssignmentEdit.route) {
            CategoryAssignmentEditView(navController = navController)
        }
        composable(Screen.SettingScreen.NotificationListenerSetting.route) {
            NotificationListenerSettingView(navController = navController)
        }

        //OCR用のスクリーン
        composable(Screen.GlobalScreen.OcrRead.route) {
            OCRView(navController = navController)
        }

        //NotificationListener用のスクリーン
        composable(Screen.GlobalScreen.NotificationListenerProcess.route) {
            NotificationListenerProcessView(navController = navController)
        }
    }
}