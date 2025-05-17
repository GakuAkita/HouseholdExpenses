package gaku.original.myapplication.ui.view.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.Screen
import gaku.original.myapplication.data.Constants.Status.SignInResult
import gaku.original.myapplication.data.Constants.Status.SingUpResult
import gaku.original.myapplication.ui.view.TopBarView
import gaku.original.myapplication.viewModel.AuthManagerViewModel
import kotlinx.coroutines.launch


@Composable
fun LoginSignUpView(
    authViewModel: AuthManagerViewModel = hiltViewModel(),
    navController: NavHostController,
    isLogin: Boolean
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val snackBarHostState = remember {
        SnackbarHostState()
    }

    Scaffold(
        topBar = {
            TopBarView(
                title = if (isLogin) "Login" else "SignUp",
                showBackButton = true,
                onBackNavClicked = { navController.popBackStack() })
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
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
                if (isLogin) {//Login画面の場合の処理
                    authViewModel.signIn(
                        email = email,
                        password = password,
                        callback = { status ->
                            when (status) {
                                SignInResult.SUCCESS -> {
                                    scope.launch {
                                        snackBarHostState.showSnackbar("ログインしました")
                                    }
                                    navController.navigate(Screen.MainScreen.Content.route) {
                                        //ログイン画面をスタックから削除して、MainScreen.Contentが一番上に来るように。
                                        popUpTo(0) {
                                            inclusive = true
                                        }
                                    }
                                }

                                SignInResult.USER_ID_NULL -> {
                                    //ログインしたが、ユーザーIDが空
                                    scope.launch {
                                        snackBarHostState.showSnackbar("ログインしましたがユーザーIDが空です\nログアウトします")
                                    }
                                    //これログアウトしてしまいたいな。
                                }

                                SignInResult.SIGN_IN_FAILED -> {
                                    scope.launch {
                                        snackBarHostState.showSnackbar("ログインに失敗しました")
                                    }
                                }
                            }
                        }
                    )

                } else {//SignUpの場合の処理
                    authViewModel.signUp(
                        email = email,
                        password = password,
                        onInitialDataAddFailed = {
                            /* サインアップは成功したが、初期データの追加ができなかった。 */
                        },
                        callback = { status ->
                            when (status) {
                                SingUpResult.SUCCESS -> {
                                    scope.launch {
                                        snackBarHostState.showSnackbar("アカウントを作成しました。ログインします")
                                    }
                                    //SignUpができたら即ログインする。
                                    authViewModel.signIn(
                                        email = email,
                                        password = password,
                                        callback = { signInStatus ->
                                            when (signInStatus) {
                                                SignInResult.SUCCESS -> {
                                                    navController.navigate(Screen.MainScreen.Content.route)
                                                }

                                                SignInResult.USER_ID_NULL -> {
                                                    //ログアウトする
                                                    scope.launch {
                                                        snackBarHostState.showSnackbar("アカウント作成しましたが、ユーザーIDが空です")
                                                    }
                                                }

                                                SignInResult.SIGN_IN_FAILED -> {
                                                    scope.launch {
                                                        snackBarHostState.showSnackbar("アカウント作成しましたが、ログインに失敗しました")
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }

                                SingUpResult.USER_ID_NULL -> {
                                    scope.launch {
                                        snackBarHostState.showSnackbar("アカウント作成しましたが、ユーザーIDが空です")
                                    }
                                }

                                SingUpResult.SIGN_UP_FAILED -> {
                                    scope.launch {
                                        snackBarHostState.showSnackbar("アカウント作成に失敗しました")
                                    }
                                }
                            }
                        }
                    )
                }
            }
            ) {
                if (isLogin) {
                    Text("Login")
                } else {
                    Text("SignUp")
                }
            }
        }
    }
}