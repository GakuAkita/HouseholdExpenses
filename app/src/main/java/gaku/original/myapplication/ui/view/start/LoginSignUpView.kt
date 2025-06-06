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
import gaku.original.myapplication.data.Constants.Status.SignInResult
import gaku.original.myapplication.data.Constants.Status.SignUpResult
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
            else -> "ログインに失敗しました。"
        }
        return message
    }

    fun getSignUpErrorMsgFromCode(errorCode: String?): String {
        val message = when (errorCode) {
            "ERROR_INVALID_EMAIL" -> "メールアドレスの形式が正しくありません。"
            "ERROR_WEAK_PASSWORD" -> "パスワードが弱すぎます。6文字以上にしてください。"
            "ERROR_OPERATION_NOT_ALLOWED" -> "この操作は許可されていません。"
            else -> ""
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
                                    }

                                    SuspendFuncStatus.FAILED -> {
                                        val errorMsg = getSignInErrorMsgFromCode(status.errorCode)
                                        scope.launch {
                                            scope.launch {
                                                snackBarHostState.showSnackbar(
                                                    errorMsg,
                                                    actionLabel = "OK"
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    } else {//SignUpの場合の処理
                        authViewModel.signUpWithCallback(
                            email,
                            password,
                            callback = {status->
                                when(status.status){
                                    SuspendFuncStatus.SUCCESS->{
                                        scope.launch {
                                            snackBarHostState.showSnackbar(
                                                "アカウントを作成しました。ログインします",
                                                actionLabel = "OK")
                                        }

                                    }

                                    SuspendFuncStatus.TIMEOUT->{
                                        scope.launch {

                                        }
                                    }
                                    SuspendFuncStatus.FAILED->{
                                        scope.launch {

                                        }

                                    }                                    }
                                }
                            }}
                        )
//                        authViewModel.signUp(
//                            email = email,
//                            password = password,
//                            callback = { status ->
//                                when (status) {
//                                    SignUpResult.SUCCESS -> {
//                                        scope.launch {
//                                            snackBarHostState.showSnackbar("アカウントを作成しました。ログインします")
//                                        }
//                                        //SignUpができたら即ログインする。
//                                        authViewModel.signIn(
//                                            email = email,
//                                            password = password,
//                                            callback = { signInStatus ->
//                                                when (signInStatus) {
//                                                    SignInResult.SUCCESS -> {
//                                                        navController.navigate(Screen.MainScreen.Content.route)
//                                                    }
//
//                                                    SignInResult.USER_ID_NULL -> {
//                                                        //ログアウトする
//                                                        scope.launch {
//                                                            snackBarHostState.showSnackbar("アカウント作成しましたが、ユーザーIDが空です")
//                                                        }
//                                                    }
//
//                                                    SignInResult.SIGN_IN_FAILED -> {
//                                                        scope.launch {
//                                                            snackBarHostState.showSnackbar("アカウント作成しましたが、ログインに失敗しました")
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        )
//                                    }
//
//                                    SignUpResult.USER_ID_NULL -> {
//                                        scope.launch {
//                                            snackBarHostState.showSnackbar("アカウント作成しましたが、ユーザーIDが空です")
//                                        }
//                                    }
//
//                                    SignUpResult.SIGN_UP_FAILED -> {
//                                        scope.launch {
//                                            snackBarHostState.showSnackbar("アカウント作成に失敗しました")
//                                        }
//                                    }
//                                }
//                                loading = false
//                            }
//                        )
//                    }
                }
                ) {
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