package gaku.original.myapplication.ui.view


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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
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
    title: String,
    showBackButton: Boolean = false,
    onBackNavClicked: () -> Unit = {},
    navController: NavController? = null
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        title = { Text(text = title, fontSize = 16.sp) },
        navigationIcon = {
//            if (navController != null) {
//                //現在のルートを取得
//                val navBackStateEntry by navController.currentBackStackEntryAsState()
//                val currentRoute = navBackStateEntry?.destination?.route
//                //ExpenseAddEditViewからMainScreenContentに戻るとき
//                //ExpenseAddEditViewだけ表示する

            if (showBackButton) IconButton(onClick = onBackNavClicked) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                    contentDescription = "back",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    )
}

@Composable
fun BottomBarView(
    navController: NavController
) {
    val bottomNavigationItems = listOf(
        BottomNavigationItem(
            title = "Main",
            icon = painterResource(id = R.drawable.baseline_home_24),
            //@Todo できればExpenseAddEditViewのとき別のbottomBarViewに移動したときに戻った際に入力結果を保存してかつ、AddEditViewに戻ってほしい
            //viewModelに保存しておくのがまるいか？
            route = Screen.MainScreen.Content.route
        ),
        BottomNavigationItem(
            title = "Graph",
            icon = painterResource(id = R.drawable.baseline_pie_chart_24),
            route = Screen.GraphScreen.route
        ),
//        BottomNavigationItem(
//            title="Not-Categorized",
//            icon= painterResource(id = R.drawable.baseline_category_24),
//            route=Screen.NotCategorizedScreen.route
//        ),
        BottomNavigationItem(
            title = "Settings",
            icon = painterResource(id = R.drawable.baseline_settings_24),
            route = Screen.SettingScreen.Main.route
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
                    navController.navigate(item.route) {}
                    /*  */
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = false
                            saveState = false
                        }
                        launchSingleTop
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
