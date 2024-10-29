package gaku.original.myapplication.ui.theme

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun GraphView(navController: NavController)
{
    Scaffold(
        topBar = {
            TopBarView("GraphView作成中")
        },

        bottomBar = { BottomBarView(navController)}
    ){
        innerPadding ->
        Text("${innerPadding}")
    }
}