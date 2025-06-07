package gaku.original.myapplication.ui.view.start

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.Screen
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
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

    var loading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackBarHostState = remember {
        SnackbarHostState()
    }

    fun getSignInErrorMsgFromCode(errorCode: String?): String {
        val message = when (errorCode) {
            "ERROR_INVALID_EMAIL" -> "メールアドレスの形式が正しくありません。"
            "ERROR_USER_DISABLED" -> "このアカウントは無効です。"
            "ERROR_USER_NOT_FOUND" -> "ユーザーが存在しません。"
            "ERROR_WRONG_PASSWORD" -> "パスワードが間違っています。"
            "_EMAIL_NOT_VERIFIED" -> "メールアドレスが認証されていません。認証メールを再送します。"
            else -> "ログインに失敗しました。"
        }
        return message
    }

    fun getSignUpErrorMsgFromCode(errorCode: String?): String {
        val message = when (errorCode) {
            "ERROR_INVALID_EMAIL" -> "メールアドレスの形式が正しくありません。"
            "ERROR_WEAK_PASSWORD" -> "パスワードが弱すぎます。6文字以上にしてください。"
            "ERROR_OPERATION_NOT_ALLOWED" -> "この操作は許可されていません。"
            else -> "アカウント作成に失敗しました"
        }
        return message
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

            if (!loading) {
                Button(onClick = {
                    loading = true
                    Log.d("LoginSignUpView", "LoginSignUpView: ボタンが押されました。")
                    if (isLogin) {//Login画面の場合の処理
                        //ログイン作業
                        authViewModel.signInWithCallback(
                            email = email,
                            password = password,
                            callback = { status ->
                                when (status.status) {
                                    SuspendFuncStatus.SUCCESS -> {
                                        scope.launch {
                                            snackBarHostState.showSnackbar(
                                                "ログインしました",
                                                actionLabel = "OK"
                                            )
                                        }
                                        navController.navigate(Screen.MainScreen.Content.route) {
                                            //ログイン画面をスタックから削除して、MainScreen.Contentが一番上に来るように。
                                            popUpTo(0) {
                                                inclusive = true
                                            }
                                        }
                                    }

                                    SuspendFuncStatus.TIMEOUT -> {
                                        scope.launch {
                                            snackBarHostState.showSnackbar(
                                                "ログインできずタイムアウトしました",
                                                actionLabel = "OK"
                                            )
                                        }
                                        loading = false
                                    }

                                    SuspendFuncStatus.FAILED -> {
                                        val errorMsg = getSignInErrorMsgFromCode(status.errorCode)
                                        scope.launch {
                                            snackBarHostState.showSnackbar(
                                                errorMsg,
                                                actionLabel = "OK"
                                            )
                                        }
                                        loading = false
                                    }
                                }
                            }
                        )
                    } else {//SignUpの場合の処理
                        //サインアップ作業
                        authViewModel.signUpWithCallback(
                            email,
                            password,
                            callback = { status ->
                                when (status.status) {
                                    SuspendFuncStatus.SUCCESS -> {
                                        scope.launch {
                                            snackBarHostState.showSnackbar(
                                                "アカウントを作成しました。メールアドレス認証をしてください。\nログイン画面に遷移します",
                                                actionLabel = "OK"
                                            )
                                            //ユーザーにスナックバーを読ませる少し待つ。
                                            kotlinx.coroutines.delay(2000L)
                                            //もし途中で他の画面に行ってしまった場合どうなるだろう？
                                            navController.navigate(Screen.StartScreen.Login.route) {
                                                //ログイン画面をスタックから削除して、MainScreen.Contentが一番上に来るように。
                                                popUpTo(0) {
                                                    inclusive = true
                                                }
                                            }
                                        }
                                    }

                                    SuspendFuncStatus.TIMEOUT -> {
                                        scope.launch {
                                            snackBarHostState.showSnackbar(
                                                "タイムアウトしました。アカウント作成に失敗しました",
                                                actionLabel = "OK"
                                            )
                                        }
                                        loading = false
                                    }

                                    SuspendFuncStatus.FAILED -> {
                                        scope.launch {
                                            val errorMsg =
                                                getSignUpErrorMsgFromCode(status.errorCode)
                                            snackBarHostState.showSnackbar(
                                                errorMsg,
                                                actionLabel = "OK"
                                            )
                                        }
                                        loading = false
                                    }
                                }
                            }
                        )
                    }
                })
                {
                    if (isLogin) {
                        Text("Login")
                    } else {
                        Text("SignUp")
                    }
                }
            } else {
                CircularProgressIndicator()
            }
        }
    }
}