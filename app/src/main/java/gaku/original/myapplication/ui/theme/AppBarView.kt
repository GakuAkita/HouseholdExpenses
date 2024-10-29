package gaku.original.myapplication.ui.theme


import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import gaku.original.myapplication.R
import gaku.original.myapplication.Screen

data class BottomNavigationItem(
    val title:String,
    val icon:Painter,
    val route:String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarView(
    title:String,
){
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.DarkGray,
            titleContentColor = Color.White
        ),
        title={Text(text=title, fontSize = 16.sp)}
    )
}

@Composable
fun BottomBarView(
    navController: NavController
){
    val bottomNavigationItems = listOf(
        BottomNavigationItem(
            title="Main",
            icon= painterResource(id = R.drawable.baseline_home_24),
            route=Screen.MainScreen.route
        ),
        BottomNavigationItem(
            title="Graph",
            icon= painterResource(id = R.drawable.baseline_pie_chart_24),
            route=Screen.GraphScreen.route
        ),
        BottomNavigationItem(
            title="Not-Categoriezed",
            icon= painterResource(id = R.drawable.baseline_category_24),
            route=Screen.NotCategorizedScreen.route
        ),
        BottomNavigationItem(
            title="Settings",
            icon= painterResource(id = R.drawable.baseline_settings_24),
            route=Screen.SettingScreen.route
        )
    )

    NavigationBar {
        val navBackStateEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStateEntry?.destination?.route

        bottomNavigationItems.forEach{item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route){
                        //スタックが積み重なるのを防ぐ？らしい。でも遷移がうまくいかんからいいや。
//                        popUpTo(navController.graph.findStartDestination().id) {
//                            saveState = true
//                        }
//                        restoreState = true
//                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        painter = item.icon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) }
            )
        }
    }
}
