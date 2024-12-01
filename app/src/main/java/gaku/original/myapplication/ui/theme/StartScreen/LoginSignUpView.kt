package gaku.original.myapplication.ui.theme.StartScreen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import gaku.original.myapplication.Screen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginSignUpView(navController: NavHostController , isLogin:Boolean) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                    if(isLogin){
                        Text("Login")
                    }else {
                        Text("SignUp")
                    }
                              },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("メールアドレス") }
            )
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("パスワード") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { performSignUp(email, password, navController) }) {
                if(isLogin){
                    Text("Login")
                }else{
                    Text("新規登録")
                }
            }
        }
    }
}

private fun performLogin(email: String, password: String, navController: NavController) {
    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uid:String?=FirebaseAuth.getInstance().currentUser?.uid
                Log.d("performLogin","${uid}")
                // ログイン成功、ホーム画面に遷移
                navController.navigate(Screen.MainScreen.Content.route)
            } else {
                // エラーハンドリング
            }
        }
}

private fun performSignUp(email: String, password: String,navController: NavHostController) {
    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("performSignUp","Created a user with Email:$email")

                // 登録成功、ログイン画面に遷移
                navController.navigate("Login")
            } else {
                // エラーハンドリング
                val errorMessage = task.exception?.message ?: "Unknown error occurred"
                Log.d("LoginView", "$errorMessage")
            }
        }
}