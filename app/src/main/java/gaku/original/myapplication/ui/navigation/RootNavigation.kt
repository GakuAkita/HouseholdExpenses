package gaku.original.myapplication.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import gaku.original.myapplication.AuthGraph
import gaku.original.myapplication.MainGraph
import gaku.original.myapplication.Splash
import gaku.original.myapplication.di.appContainer.AppContainer
import gaku.original.myapplication.domain.AuthState
import gaku.original.myapplication.ui.screens.RootUiEffect
import gaku.original.myapplication.ui.screens.RootViewModel
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

@Composable
fun RootNavigation(
    navHostController: NavHostController,
    appContainer: AppContainer,
    viewModel: RootViewModel = viewModel(factory = RootViewModel.Factory)
) {
    val authState by viewModel.authState.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(authState) {
        Timber.d("Effect started authState=${authState}\n current=${navHostController.currentBackStackEntry?.destination?.route}")
        when (authState) {
            is AuthState.Loading -> {

            }

            is AuthState.LoggedIn -> {
                val isInMainGraph =
                    navHostController.currentDestination?.hierarchy?.any { it.hasRoute<MainGraph>() } == true
                if (!isInMainGraph) {
                    /* if it doesn't check whether maingraph or not, HomeViewModel is recreated after the screen rotation */
                    /* By checking if MainGraph still exists in the tree, we can avoid recreating HomeViewModel */
                    /* This seems to be an anti-pattern...? */
                    appContainer.createSession()
                    navHostController.navigate(MainGraph) {
                        Timber.d("navigate to main. Remove all the stacks until ${navHostController.graph.id}")
                        popUpTo(navHostController.graph.id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }

            is AuthState.LoggedOut -> {
                val isAuthGraph =
                    navHostController.currentDestination?.hierarchy?.any { it.hasRoute<AuthGraph>() } == true
                if (!isAuthGraph) {
                    Timber.d("Logged out! Move to the Start Screen")
                    appContainer.clearSession()
                    navHostController.navigate(AuthGraph.Start) {
                        popUpTo(navHostController.graph.id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is RootUiEffect.ExpenseAdd -> {
                    val newExpense = event.expense
                    Timber.d("ExpenseAdd: $newExpense")
                    navHostController.navigate(
                        MainGraph.Global.ExpenseAddEdit(
                            newExpense
                        )
                    )
                }

                is RootUiEffect.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    NavHost(
        navController = navHostController,
        startDestination = Splash,
    ) {
        composable<Splash> {
            Scaffold { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
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