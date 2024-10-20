package gaku.original.myapplication.ui.theme

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BottomNavigationItem(
    val title:String,
    val selectedIcon: ImageVector,
    val unselectedIcon:ImageVector,
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
            "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Filled.Home
        ),
        BottomNavigationItem(
            "Graph",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Filled.Home
        ),
        BottomNavigationItem(
            "Not Categoriezed",
            selectedIcon = Icons.Filled.Notifications,
            unselectedIcon = Icons.Filled.Notifications
        ),
        BottomNavigationItem(
            "Settings",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Filled.Settings
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
                        imageVector = if(index==selectedItemIndex)item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title
                    )
                }
            )
        }
    }
}
