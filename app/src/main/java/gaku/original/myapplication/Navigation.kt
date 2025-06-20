package gaku.original.myapplication

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import gaku.original.myapplication.ui.view.main.CategoryAddEditView
import gaku.original.myapplication.ui.view.main.ExpenseAddEditView
import gaku.original.myapplication.ui.view.main.GraphView
import gaku.original.myapplication.ui.view.main.MainView
import gaku.original.myapplication.ui.view.main.NotCategorizedView
import gaku.original.myapplication.ui.view.settings.SettingsView
import gaku.original.myapplication.ui.view.settings.menu.AppSettingsView
import gaku.original.myapplication.ui.view.settings.menu.GmailLinking.GmailLinkingView
import gaku.original.myapplication.ui.view.settings.menu.GmailLinking.RakutenPaySettingView
import gaku.original.myapplication.ui.view.settings.menu.RepeatAddSettingView
import gaku.original.myapplication.ui.view.settings.menu.UserInfoView
import gaku.original.myapplication.ui.view.start.ForgotPasswordView
import gaku.original.myapplication.ui.view.start.LoginSignUpView
import gaku.original.myapplication.ui.view.start.StartView


@Composable
fun Navigation(
) {
    val navController = rememberNavController()

    //サインインすでにしているかをみたい
    val firebaseUser = FirebaseAuth.getInstance().currentUser

    /**
     * ん～なんかちょっと変。構造が。
     */
    /* こうすることで、再起動前にログインしていた場合、MainViewに直接飛ぶ */
    val startDestination: String = if (firebaseUser != null && firebaseUser.isEmailVerified) {
        Screen.MainScreen.Content.route
    } else {
        Screen.StartScreen.Start.route

    }

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

        composable(Screen.MainScreen.ExpenseAddEdit.route) {
            ExpenseAddEditView(navController = navController)
        }
        composable(Screen.MainScreen.CategoryAddEdit.route) {
            CategoryAddEditView(navController = navController)
        }

        //Graphスクリーン
        composable(Screen.GraphScreen.route) {
            GraphView(navController = navController)
        }

        //NotCategorizedスクリーン
        composable(Screen.NotCategorizedScreen.route) {
            NotCategorizedView(navController = navController)
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
        composable(Screen.SettingScreen.GmailLinking.Main.route) {
            GmailLinkingView(navController = navController)
        }
        composable(Screen.SettingScreen.GmailLinking.RakutenPay.route) {
            RakutenPaySettingView(navController = navController)
        }
        composable(Screen.SettingScreen.GmailLinking.AmazonKindle.route) {

        }
        composable(Screen.SettingScreen.GmailLinking.AmazonItem.route) {

        }
        composable(Screen.SettingScreen.GmailLinking.ShikokuElectricPower.route) {
        }
    }
}