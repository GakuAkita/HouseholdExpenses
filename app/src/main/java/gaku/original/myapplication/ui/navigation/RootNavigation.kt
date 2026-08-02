package gaku.original.myapplication.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.room.util.TableInfo
import gaku.original.myapplication.AuthGraph
import gaku.original.myapplication.Splash
import gaku.original.myapplication.di.appContainer.AppContainer

@Composable
fun RootNavigation(
    navHostController: NavHostController,
    appContainer: AppContainer
){
    NavHost(
        navController = navHostController,
//        startDestination = Splash,
        startDestination = AuthGraph// Only for Debugging
    ){
        composable<Splash> {
            Scaffold(

            ) {innerPadding->
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LinearProgressIndicator()
                }
            }
        }

        authGraph(navHostController)
        mainGraph(navHostController)
    }
}