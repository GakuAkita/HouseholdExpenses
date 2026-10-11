package gaku.original.myapplication.ui.common


import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import gaku.original.myapplication.MainGraph
import gaku.original.myapplication.R
import gaku.original.myapplication.Screen

data class BottomNavigationItem(
    val title: String,
    val icon: Painter,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarView(
    title: String = "What is essential is invisible to the eye",
    showBackButton: Boolean = false,
    onBackNavClicked: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (showBackButton) IconButton(onClick = onBackNavClicked) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "back"
                )
            }
        },
        actions = actions
    )
}

@Composable
fun BottomBarView(
    navController: NavController
) {
    // @Todo ちゃんと設定する
    val bottomNavigationItems = listOf(
        BottomNavigationItem(
            title = "Main",
            icon = painterResource(id = R.drawable.baseline_home_24),
            //@Todo できればExpenseAddEditViewのとき別のbottomBarViewに移動したときに戻った際に入力結果を保存してかつ、AddEditViewに戻ってほしい
            //viewModelに保存しておくのがまるいか？
            route = MainGraph.Bottom.toString()
        ),
        BottomNavigationItem(
            title = "Search",
            icon = painterResource(id = R.drawable.baseline_search_24),
            route = MainGraph.Bottom.toString()
        ),
        BottomNavigationItem(
            title = "Graph",
            icon = painterResource(id = R.drawable.baseline_pie_chart_24),
            route = MainGraph.Bottom.toString()
        ),
        BottomNavigationItem(
            title = "Settings",
            icon = painterResource(id = R.drawable.baseline_settings_24),
            route = MainGraph.Bottom.toString()
        )
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        val navBackStateEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStateEntry?.destination?.route

        bottomNavigationItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    /*  */
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = false
                            saveState = false
                        }
                        launchSingleTop = true
                        //スタックが積み重なるのを防ぐ？らしい。でも遷移がうまくいかんからいいや。
////                        popUpTo(navController.graph.findStartDestination().id) {
////                            saveState = true
////                        }
////                        restoreState = true
////                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        painter = item.icon,
                        contentDescription = item.title,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                },
                label = { Text(item.title, color = MaterialTheme.colorScheme.onPrimary) },
            )
        }
    }
}
