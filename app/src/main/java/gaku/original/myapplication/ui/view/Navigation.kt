package gaku.original.myapplication.ui.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import gaku.original.myapplication.AuthGraph


@Composable
fun Navigation(
    navController: NavHostController,
    startDestination: String = AuthGraph.Start.toString()
) {
    NavHost(
        modifier = Modifier.fillMaxSize()/* これをつけるとnavigateすると左上から右下に行くやつがなくなる？　*/,
        navController = navController,
        startDestination = startDestination
    ) {

        //Startスクリーン
//        composable(Screen.StartScreen.Start.route) {
//            StartView(navController = navController)
//        }
//        composable(Screen.StartScreen.SignUp.route) {
//            val isLogin = false
//            LoginSignUpView(
//                navController = navController,
//                isLogin = isLogin
//            )
//        }
//        composable(Screen.StartScreen.Login.route) {
//            val isLogin = true
//            LoginSignUpView(
//                navController = navController,
//                isLogin = isLogin,
//                googleOnly = true
//            )
//        }
//        composable(Screen.StartScreen.ForgotPassword.route) {
//            ForgotPasswordView(navController = navController)
//        }
//
//        //Mainスクリーン
//        composable(Screen.MainScreen.Content.route)
//        {
//            MainView(navController = navController)
//        }
//
//        composable(route = Screen.GlobalScreen.ExpenseAddEdit.route, arguments = listOf(
//            navArgument("from") {
//                type = NavType.StringType
//                defaultValue = "unknown"
//            }
//        )) { backStackEntry ->
//            val from = backStackEntry.arguments?.getString("from") ?: "unknown"
//            ExpenseAddEditView(navController = navController, from = from)
//        }
//        composable(Screen.GlobalScreen.CategoryAddEdit.route) {
//            CategoryAddEditView(navController = navController)
//        }
//
//        //Graphスクリーン
//        composable(Screen.GraphScreen.route) {
//            GraphView(navController = navController)
//        }
//
//        //NotCategorizedスクリーン
//        composable(Screen.SearchScreen.route) {
//            SearchView(navController = navController)
//        }
//
//        //Settingsスクリーン
//        composable(Screen.SettingScreen.Main.route) {
//            SettingsView(navController = navController)
//        }
//        composable(Screen.SettingScreen.UserInfo.route) {
//            UserInfoView(navController = navController)
//        }
//        composable(Screen.SettingScreen.RepeatAdd.route) {
//            RepeatAddSettingView(navController = navController)
//        }
//        composable(Screen.SettingScreen.AppSettings.route) {
//            AppSettingsView(navController = navController)
//        }
//        composable(Screen.SettingScreen.MailboxExtraction.Main.route) {
//            MailboxExtractionView(navController = navController)
//        }
//        composable(Screen.GlobalScreen.CategoryAssignmentEdit.route) {
//            CategoryAssignmentEditView(navController = navController)
//        }
//        composable(Screen.SettingScreen.NotificationListenerSetting.route) {
//            NotificationListenerSettingView(navController = navController)
//        }
//
//        composable(Screen.SettingScreen.PayPayReceiptOCRSetting.route) {
//            PayPayReceiptOCRSettingView(navController = navController)
//        }
//
//        composable(Screen.SettingScreen.AmazonSubscribeItems.route) {
//            AmazonSubscribeItemsView(navController = navController)
//        }
//
//        composable(Screen.SettingScreen.Version.route) {
//            VersionView(navController = navController)
//        }
//
//        //OCR用のスクリーン
//        composable(Screen.GlobalScreen.OCR.Entry.route) {
//            OCREntryView(navController = navController)
//        }
//        composable(Screen.GlobalScreen.OCR.MaskRatioAdjust.route) {
//            OCRMaskRatioAdjustView(navController = navController)
//        }
//        composable(Screen.GlobalScreen.OCR.Read.route) {
//            OCRView(navController = navController)
//        }
//
//        //NotificationListener用のスクリーン
//        composable(Screen.GlobalScreen.NotificationListenerProcess.route) {
//            NotificationListenerProcessView(navController = navController)
//        }
    }
}