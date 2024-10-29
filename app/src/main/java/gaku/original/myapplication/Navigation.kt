package gaku.original.myapplication

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import gaku.original.myapplication.ui.theme.GraphView
import gaku.original.myapplication.ui.theme.MainView
import gaku.original.myapplication.ui.theme.NotCategorizedView
import gaku.original.myapplication.ui.theme.SettingsView

@Composable
fun Navigation(
    viewModel: ExpenseViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
){
    NavHost(
        navController = navController,
        startDestination = Screen.MainScreen.route
    ){
        //Mainスクリーン
        composable(Screen.MainScreen.route){
            MainView(viewModel,navController)
        }

        composable(Screen.GraphScreen.route){
            GraphView(navController)
        }

        composable(Screen.NotCategorizedScreen.route){
            NotCategorizedView()
        }

        composable(Screen.SettingScreen.route){
            SettingsView()
        }
    }
}