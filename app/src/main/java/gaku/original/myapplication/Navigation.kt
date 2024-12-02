package gaku.original.myapplication

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import gaku.original.myapplication.ui.theme.GraphView
import gaku.original.myapplication.ui.theme.NotCategorizedView
import gaku.original.myapplication.ui.theme.SettingsView
import gaku.original.myapplication.ui.theme.mainScreen.AddEditView
import gaku.original.myapplication.ui.theme.mainScreen.MainView
import gaku.original.myapplication.ui.theme.startScreen.LoginSignUpView
import gaku.original.myapplication.ui.theme.startScreen.StartView

@Composable
fun Navigation(
    ExpenseViewModel: ExpenseViewModel = hiltViewModel(),
    navController: NavHostController = rememberNavController()
){
    NavHost(
        navController = navController,
        startDestination = Screen.StartScreen.Start.route
    ){
        //Startスクリーン
        composable(Screen.StartScreen.Start.route){
            StartView(navController)
        }
        composable(Screen.StartScreen.SignUp.route){
            val isLogin = false
            LoginSignUpView(ExpenseViewModel,navController,isLogin)
        }
        composable(Screen.StartScreen.Login.route){
            val isLogin = true
            LoginSignUpView(ExpenseViewModel,navController,isLogin)
        }

        //Mainスクリーン
        composable(Screen.MainScreen.Content.route){
            MainView(ExpenseViewModel,navController)
        }
        composable(Screen.MainScreen.AddEdit.route){
            AddEditView(ExpenseViewModel,navController)
        }

        //Graphスクリーン
        composable(Screen.GraphScreen.route){
            GraphView(navController)
        }

        //NotCategorizedスクリーン
        composable(Screen.NotCategorizedScreen.route){
            NotCategorizedView(navController)
        }

        //Settingsスクリーン
        composable(Screen.SettingScreen.route){
            SettingsView(navController)
        }
    }
}