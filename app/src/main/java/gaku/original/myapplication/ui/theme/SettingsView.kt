package gaku.original.myapplication.ui.theme

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun SettingsView(navController: NavController){
    Scaffold(
        topBar = {
            TopBarView("SettingsView作成中")
        },

        bottomBar = { BottomBarView(navController)}
    ){
            innerPadding ->
        Text("${innerPadding}")
        Text("This App will never be completed. It will continue to grow as long as there is imagination left in the world.")
    }
}