package gaku.original.myapplication.ui.screens.bottom

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.MainGraph
import gaku.original.myapplication.Screen
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.ui.navigation.navigateToBottom
import gaku.original.myapplication.ui.screens.bottom.home.HomeScreenRoot
import gaku.original.myapplication.ui.screens.bottom.home.HomeViewModel
import gaku.original.myapplication.ui.screens.bottom.search.SearchScreenRoot
import gaku.original.myapplication.ui.screens.bottom.setting.SettingScreenRoot
import gaku.original.myapplication.ui.screens.bottom.setting.SettingViewModel
import gaku.original.myapplication.ui.screens.bottom.statistics.StatisticsScreenRoot

private data class BottomTab(
    val route: Screen,
    val icon: ImageVector,
    val label: String,
    val contentDescription: String
)

private val bottomTabs = listOf(
    BottomTab(MainGraph.Bottom.Home, Icons.Default.Home, "Home", "Home"),
    BottomTab(MainGraph.Bottom.Search, Icons.Default.Search, "Search", "Search"),
    BottomTab(MainGraph.Bottom.Statistics, Icons.Default.Analytics, "Statistics", "Statistics"),
    BottomTab(MainGraph.Bottom.Setting, Icons.Default.Settings, "Setting", "Settings")
)

@Composable
fun MainFrame(
    rootNavController: NavHostController
) {
    val bottomNavController = rememberNavController()

    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val snackbarHostState = LocalSnackBarHostState.current

    // 横向きのときはボトムバーの代わりに右側にNavigationRailを表示する
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            TopBarView("What is essential is invisible to the eye")
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        bottomBar = {
            if (!isLandscape) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentDestination?.hasRoute(tab.route::class) == true,
                            onClick = {
                                bottomNavController.navigateToBottom(tab.route)
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.contentDescription
                                )
                            },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                modifier = Modifier.weight(1f),
                navController = bottomNavController,
                startDestination = MainGraph.Bottom.Home
            ) {
                composable<MainGraph.Bottom.Home> { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        rootNavController.getBackStackEntry(MainGraph)
                    }
                    val viewModel = viewModel<HomeViewModel>(
                        viewModelStoreOwner = parentEntry,
                        factory = HomeViewModel.Factory
                    )
                    HomeScreenRoot(
                        rootNavController = rootNavController,
                        viewModel = viewModel
                    )
                }
                composable<MainGraph.Bottom.Search> {
                    SearchScreenRoot(
                        rootNavController = rootNavController
                    )
                }
                composable<MainGraph.Bottom.Statistics> {
                    StatisticsScreenRoot()
                }
                composable<MainGraph.Bottom.Setting> { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        rootNavController.getBackStackEntry(MainGraph)
                    }
                    val viewModel = viewModel<SettingViewModel>(
                        viewModelStoreOwner = parentEntry,
                        factory = SettingViewModel.Factory
                    )

                    SettingScreenRoot(
                        rootNavController = rootNavController,
                        viewModel = viewModel
                    )
                }
            }

            if (isLandscape) {
                // Scaffoldのinnerpaddingでシステムバー分は確保済みなのでinsetsは0にする
                NavigationRail(windowInsets = WindowInsets(0)) {
                    Spacer(Modifier.weight(1f))
                    bottomTabs.forEach { tab ->
                        NavigationRailItem(
                            selected = currentDestination?.hasRoute(tab.route::class) == true,
                            onClick = {
                                bottomNavController.navigateToBottom(tab.route)
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.contentDescription
                                )
                            },
                            label = { Text(tab.label) }
                        )
                    }
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}
