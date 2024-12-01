package gaku.original.myapplication.ui.theme.StartScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import gaku.original.myapplication.Screen

@Composable
fun StartView(navController: NavHostController){
    Column(
        modifier= Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ){
        Text(text="The App will never be completed.\n" +
                " It will continue to grow as long as there is imagination left in the world.",
            textAlign = TextAlign.Center
        )

        Spacer(modifier=Modifier.size(10.dp))

        Text(text="このアプリは永遠に完成しない。\n"+
                    "この世界に想像力が残っている限り、成長し続ける。",
            textAlign = TextAlign.Center
        )

        Spacer(modifier=Modifier.size(10.dp))

        Row (
            modifier=Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            val buttonWidth=150
            Button(
                modifier = Modifier.width(buttonWidth.dp),
                onClick = {
                    navController.navigate(Screen.StartScreen.SignUp.route)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Sign Up")
            }

            Button(
                modifier = Modifier.width(buttonWidth.dp),
                onClick = {
                    navController.navigate(Screen.StartScreen.Login.route)
                }
            ) {
                Text("Login")
            }
        }
    }
}
