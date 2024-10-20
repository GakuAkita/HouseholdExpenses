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
import gaku.original.myapplication.R

data class BottomNavigationItem(
    val title:String,
    val icon:Painter
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarView(
    title:String
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
fun BottomBarView(){
    var selectedItemIndex by rememberSaveable { mutableStateOf(0) }
    val bottomNavigationItems = listOf(
        BottomNavigationItem(
            title="Home",
            icon= painterResource(id = R.drawable.baseline_home_24)
        ),
        BottomNavigationItem(
            title="Graph",
            icon= painterResource(id = R.drawable.baseline_pie_chart_24)
        ),
        BottomNavigationItem(
            title="Not-Categoriezed",
            icon= painterResource(id = R.drawable.baseline_category_24)
        ),
        BottomNavigationItem(
            title="Settings",
            icon= painterResource(id = R.drawable.baseline_settings_24)
        )
    )

    NavigationBar {
        bottomNavigationItems.forEachIndexed{index,item ->
            NavigationBarItem(
                selected = selectedItemIndex == index,
                onClick = {
                    selectedItemIndex = index
                    //navigation....
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
