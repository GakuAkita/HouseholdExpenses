package gaku.original.myapplication.ui.screens.bottom

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.MainGraph
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.ui.screens.bottom.home.HomeScreenRoot

@Composable
fun MainFrame(
    rootNavController: NavHostController
) {
    val bottomNavController = rememberNavController()

    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val snackbarHostState = LocalSnackBarHostState.current

    Scaffold(
        topBar = {
            TopBarView("What is essential is invisible to the eye")
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        bottomBar = {
            NavigationBar() {
                NavigationBarItem(
                    selected = currentDestination?.hasRoute<MainGraph.Bottom.Home>() == true,
                    onClick = {

                    },
                    icon = {
                        Text("Home")
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            modifier = Modifier.padding(innerPadding),
            navController = bottomNavController,
            startDestination = MainGraph.Bottom.Home
        ) {
            composable<MainGraph.Bottom.Home> {
                HomeScreenRoot(
                    bottomNavController = bottomNavController,
                )
            }
            composable<MainGraph.Bottom.Search> {
            }
            composable<MainGraph.Bottom.Statistics> {
            }
            composable<MainGraph.Bottom.Setting> {
            }
        }
    }

}