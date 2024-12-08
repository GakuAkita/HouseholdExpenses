package gaku.original.myapplication.ui.theme

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import gaku.original.myapplication.ExpenseViewModel
import gaku.original.myapplication.Screen
import gaku.original.myapplication.SharedViewModel

@Composable
fun SettingsView(viewModel: ExpenseViewModel,navController: NavController){
    Scaffold(
        topBar = {
            TopBarView("SettingsView作成中")
        },

        bottomBar = { BottomBarView(navController)}
    ){ innerPadding ->
        val context = LocalContext.current

        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            Text("The App will never be completed. \n" +
                    "It will continue to grow as long as there is imagination left in the world.")

             Button(
                 modifier=Modifier.fillMaxWidth(),
                 onClick = {
                     //ログアウト機能を実装
                     viewModel.signOut()
                     Toast.makeText(context,"ログアウトしました",Toast.LENGTH_SHORT).show()
                     navController.navigate(Screen.StartScreen.Start.route)
                 }
             ) {
                 Text("LogOut(仮)")
             }
        }
    }
}