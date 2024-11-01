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
    bottomBarNavController: NavHostController = rememberNavController()
){
    NavHost(
        navController = bottomBarNavController,
        startDestination = Screen.MainScreen.Content.route
    ){
        //Mainスクリーン
        composable(Screen.MainScreen.Content.route){
            MainView(viewModel,bottomBarNavController)
        }

        composable(Screen.GraphScreen.route){
            GraphView(bottomBarNavController)
        }

        composable(Screen.NotCategorizedScreen.route){
            NotCategorizedView(bottomBarNavController)
        }

        composable(Screen.SettingScreen.route){
            SettingsView(bottomBarNavController)
        }
    }
}