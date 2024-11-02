package gaku.original.myapplication

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import gaku.original.myapplication.ui.theme.AddEditView
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
        startDestination = Screen.MainScreen.Content.route
    ){
        //Mainスクリーン
        composable(Screen.MainScreen.Content.route){
            MainView(viewModel,navController)
        }
        composable(Screen.MainScreen.AddEdit.route){
            AddEditView(navController)
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