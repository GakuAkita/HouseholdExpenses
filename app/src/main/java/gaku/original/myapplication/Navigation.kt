package gaku.original.myapplication

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import gaku.original.myapplication.data.ExpenseRepository
import gaku.original.myapplication.ui.view.GraphView
import gaku.original.myapplication.ui.view.NotCategorizedView
import gaku.original.myapplication.ui.view.SettingsView
import gaku.original.myapplication.ui.view.main.AddEditView
import gaku.original.myapplication.ui.view.main.MainView
import gaku.original.myapplication.ui.view.start.LoginSignUpView
import gaku.original.myapplication.ui.view.start.StartView
import gaku.original.myapplication.viewModel.AuthManagerViewModel
import gaku.original.myapplication.viewModel.ExpenseAddEditViewModel
import gaku.original.myapplication.viewModel.ExpenseListViewModel
import gaku.original.myapplication.viewModel.ExpenseSharedViewModel
import gaku.original.myapplication.viewModel.TemporaryExpenseViewModel
import gaku.original.myapplication.viewModel.UserInfoViewModel

@Composable
fun Navigation(){
    val navController = rememberNavController()

    //本当はhiltとか使いたいけど、手動DIにする。
    //@Todo hilt使えるようになる
    val userInfoViewModel = remember { UserInfoViewModel() }
    val realtimeDbReference = RealtimeDbReference(userInfoViewModel)
    val dbListenerManager = DbListenerManager(realtimeDbReference)
    val authManagerViewModel = remember {AuthManagerViewModel(userInfoViewModel)}
    val expenseRepository = ExpenseRepository(realtimeDbReference)
    val expenseSharedViewModel = remember { ExpenseSharedViewModel(expenseRepository,dbListenerManager) }
    val temporaryExpenseViewModel = remember { TemporaryExpenseViewModel() }
    val expenseListViewModel = remember { ExpenseListViewModel(expenseSharedViewModel,temporaryExpenseViewModel) }
    val expenseAddEditViewModel = remember { ExpenseAddEditViewModel(expenseSharedViewModel,temporaryExpenseViewModel) }


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
            LoginSignUpView(authManagerViewModel,navController,isLogin)
        }
        composable(Screen.StartScreen.Login.route){
            val isLogin = true
            LoginSignUpView(authManagerViewModel,navController,isLogin)
        }

        //Mainスクリーン
        composable(Screen.MainScreen.Content.route){
            //AuthManagerViewModelでサインイン後なのか、そうでないのかを判断
            MainView(expenseListViewModel,navController)
        }
        composable(Screen.MainScreen.AddEdit.route){
            AddEditView(expenseAddEditViewModel,navController)
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
            SettingsView(authManagerViewModel,navController)
        }
    }
}