package gaku.original.myapplication

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import gaku.original.myapplication.data.ExpenseRepository
import gaku.original.myapplication.ui.theme.GraphView
import gaku.original.myapplication.ui.theme.NotCategorizedView
import gaku.original.myapplication.ui.theme.SettingsView
import gaku.original.myapplication.ui.theme.mainScreen.AddEditView
import gaku.original.myapplication.ui.theme.mainScreen.MainView
import gaku.original.myapplication.ui.theme.startScreen.LoginSignUpView
import gaku.original.myapplication.ui.theme.startScreen.StartView
import kotlin.math.exp

@Composable
fun Navigation(){
    val navController = rememberNavController()

    //本当はhiltとか使いたいけど、手動DIにする。
    //@Todo hilt使えるようになる
    val repository = ExpenseRepository()
    val expenseViewModel = remember { ExpenseViewModel(repository) }

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
            LoginSignUpView(expenseViewModel,navController,isLogin)
        }
        composable(Screen.StartScreen.Login.route){
            val isLogin = true
            LoginSignUpView(expenseViewModel,navController,isLogin)
        }

        //Mainスクリーン
        composable(Screen.MainScreen.Content.route){
            MainView(expenseViewModel,navController)
        }
        composable(Screen.MainScreen.AddEdit.route){
            AddEditView(expenseViewModel,navController)
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