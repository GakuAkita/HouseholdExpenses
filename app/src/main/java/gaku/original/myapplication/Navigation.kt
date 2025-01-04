package gaku.original.myapplication

import android.util.Log
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import gaku.original.myapplication.data.CategoryRepository
import gaku.original.myapplication.data.ExpenseRepository
import gaku.original.myapplication.ui.view.GraphView
import gaku.original.myapplication.ui.view.NotCategorizedView
import gaku.original.myapplication.ui.view.SettingsView
import gaku.original.myapplication.ui.view.main.CategoryAddEditView
import gaku.original.myapplication.ui.view.main.ExpenseAddEditView
import gaku.original.myapplication.ui.view.main.MainView
import gaku.original.myapplication.ui.view.start.LoginSignUpView
import gaku.original.myapplication.ui.view.start.StartView
import gaku.original.myapplication.viewModel.AuthManagerViewModel
import gaku.original.myapplication.viewModel.ExpenseAddEditViewModel
import gaku.original.myapplication.viewModel.ExpenseListViewModel
import gaku.original.myapplication.viewModel.ExpenseSharedViewModel
import gaku.original.myapplication.viewModel.TemporaryExpenseViewModel
import gaku.original.myapplication.viewModel.UserInfoViewModel
import javax.inject.Inject


@Composable
fun Navigation (
){

    val navController = rememberNavController()

    val firebaseAuth = AppModule.provideFirebaseAuth()

    //本当はhiltとか使いたいけど、手動DIにする。
    //@Todo hilt使えるようになる


    /* こうすることで、再起動前にログインしていた場合、MainViewに直接飛ぶ */
    val startDestination :String
    if( firebaseAuth.currentUser !=null ){
        startDestination = Screen.MainScreen.Content.route
    }else{
        startDestination = Screen.StartScreen.Start.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ){

        //Startスクリーン
        composable(Screen.StartScreen.Start.route){
            StartView(navController = navController)
        }
        composable(Screen.StartScreen.SignUp.route){
            val isLogin = false
            LoginSignUpView(navController = navController, isLogin = isLogin)
        }
        composable(Screen.StartScreen.Login.route){
            val isLogin = true
            LoginSignUpView(navController = navController, isLogin = isLogin)
        }

        //Mainスクリーン
        composable(Screen.MainScreen.Content.route)
        {
            //AuthManagerViewModelでサインイン後なのか、そうでないのかを判断
            MainView(navController = navController)
        }
        composable(Screen.MainScreen.ExpenseAddEdit.route){
            ExpenseAddEditView(navController = navController)
        }
        composable(Screen.MainScreen.CategoryAddEdit.route){
            CategoryAddEditView(navController = navController)
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
            SettingsView(navController = navController)
        }
    }
}