package gaku.original.myapplication.ui.theme.startScreen

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import gaku.original.myapplication.ExpenseViewModel
import gaku.original.myapplication.Screen
import gaku.original.myapplication.data.SignInStatus
import gaku.original.myapplication.data.SignUpStatus


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginSignUpView(viewModel:ExpenseViewModel, navController: NavHostController, isLogin:Boolean) {
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
                    IconButton(onClick = {
                        navController.navigate(Screen.StartScreen.Start.route)
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) {
        val context = LocalContext.current

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
                label = { Text("mail") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("password") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(30.dp))

            Button(onClick = {
                if(isLogin){//Login画面の場合の処理
                    viewModel.signInAndFetchAllExpenses(
                        email = email,
                        password = password,
                        callback = {status ->
                            when (status){
                                SignInStatus.SUCCESS -> {
                                    //ログインしたときにExpensesを更新
                                    Toast.makeText(context,"ログインしました",Toast.LENGTH_SHORT).show()
                                    navController.navigate(Screen.MainScreen.Content.route)
                                }
                                SignInStatus.USER_ID_NULL -> {
                                    //ログインしたが、ユーザーIDが空
                                    Toast.makeText(context,"ログインしましたがユーザーIDが空です\nログアウトします",Toast.LENGTH_SHORT).show()
                                    //これログアウトしてしまいたいな。
                                }
                                SignInStatus.SIGN_IN_FAILED -> {
                                    Toast.makeText(context,"ログインに失敗しました",Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )

                }else{//SignUpの場合の処理
                    viewModel.signUpAndInitialSetup(
                        email = email,
                        password = password,
                        callback =  { status ->
                            when (status) {
                                SignUpStatus.SUCCESS -> {
                                    Toast.makeText(context, "アカウントを作成しました。ログインします", Toast.LENGTH_SHORT).show()
                                    //SignUpができたら即ログインする。
                                    viewModel.signInAndFetchAllExpenses(
                                        email = email,
                                        password = password,
                                        callback = { signInStatus ->
                                            when (signInStatus) {
                                                SignInStatus.SUCCESS -> {
                                                    navController.navigate(Screen.MainScreen.Content.route)
                                                }

                                                SignInStatus.USER_ID_NULL -> {
                                                    //ログアウトする
                                                    Toast.makeText(context, "アカウント作成しましたが、ユーザーIDが空です", Toast.LENGTH_SHORT).show()
                                                }

                                                SignInStatus.SIGN_IN_FAILED -> {
                                                    //もう一度ログインする
                                                    Toast.makeText(context, "アカウント作成しましたが、ログインに失敗しました", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    )
                                }

                                SignUpStatus.USER_ID_NULL -> {
                                    Toast.makeText(context, "アカウント作成しましたが、ユーザーIDが空です", Toast.LENGTH_SHORT).show()
                                }

                                SignUpStatus.SIGN_UP_FAILED -> {
                                    Toast.makeText(context, "アカウント作成に失敗しました", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
            ) {
                if(isLogin){
                    Text("Login")
                }else{
                    Text("SignUp")
                }
            }
        }
    }
}