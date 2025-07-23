package gaku.original.myapplication.ui.view.start

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import gaku.original.myapplication.CredentialManagerHelper
import gaku.original.myapplication.R
import gaku.original.myapplication.Screen
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.FetchResult
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.utility.LogAkitaDebug
import gaku.original.myapplication.viewModel.start.AuthManagerViewModel
import kotlinx.coroutines.launch


@Composable
fun LoginSignUpView(
    authViewModel: AuthManagerViewModel = hiltViewModel(),
    navController: NavHostController,
    isLogin: Boolean,
    googleOnly: Boolean = true
) {
    val context = LocalContext.current

    /**
     * rememberSavableはスタックから消えない限り生き残る
     */
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var isLoginState by remember { mutableStateOf(isLogin) }

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

    fun handleLogin() {
        authViewModel.signInWithCallback(
            email = email,
            password = password,
            callback = { status ->
                when (status.status) {
                    SuspendFuncStatus.SUCCESS -> {
                        scope.launch {
                            snackBarHostState.showSnackbar(
                                "ログインしました",
                                actionLabel = "OK",
                                duration = SnackbarDuration.Long
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
                                actionLabel = "OK",
                                duration = SnackbarDuration.Long
                            )
                        }
                        loading = false
                    }

                    SuspendFuncStatus.FAILED -> {
                        val errorMsg =
                            getSignInErrorMsgFromCode(status.errorCode)
                        LogAkitaDebug(errorMsg)
                        scope.launch {
                            snackBarHostState.showSnackbar(
                                errorMsg,
                                actionLabel = "OK",
                                duration = SnackbarDuration.Long
                            )
                        }
                        loading = false
                    }
                }
            }
        )
    }

    fun handleSignUp() {
        authViewModel.signUpWithCallback(
            email,
            password,
            callback = { status ->
                when (status.status) {
                    SuspendFuncStatus.SUCCESS -> {
                        scope.launch {
                            snackBarHostState.showSnackbar(
                                "アカウントを作成しました。メールアドレス認証をしてください。\n認証後、ログインボタンを押してください",
                                actionLabel = "OK",
                            )
                            //サインアップに成功したら画面がログイン画面に切り替わる
                            //※画面のUIが変わっているだけでルートは変わっていない!!
                        }
                        loading = false
                        isLoginState = true
                    }

                    SuspendFuncStatus.TIMEOUT -> {
                        scope.launch {
                            snackBarHostState.showSnackbar(
                                "タイムアウトしました。アカウント作成に失敗しました",
                                actionLabel = "OK",
                                duration = SnackbarDuration.Long
                            )
                        }
                        loading = false
                    }

                    SuspendFuncStatus.FAILED -> {
                        scope.launch {
                            val errorMsg =
                                getSignUpErrorMsgFromCode(status.errorCode)
                            LogAkitaDebug(errorMsg)
                            snackBarHostState.showSnackbar(
                                errorMsg,
                                actionLabel = "OK",
                                duration = SnackbarDuration.Long
                            )
                        }
                        loading = false
                    }
                }
            }
        )
    }

    val user by authViewModel.currentUser.collectAsState()
    val signInLoading by authViewModel.signInLoading.collectAsState()
    LaunchedEffect(user) {
        /**
         * userが変化したら走る。
         */
        if (user != null) {
            /* Mainスクリーンに遷移 */
            navController.navigate(Screen.MainScreen.Content.route)
        }
    }

    Scaffold(
        topBar = {
            TopBarView(
                title = if (isLoginState) "Login" else "SignUp",
                showBackButton = true,
                onBackNavClicked = { navController.popBackStack() })
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { innerPadding ->
        if (googleOnly) {
            /**
             * Googleログインだけに絞る場合
             */
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Googleログインのみにする", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(30.dp))
                if (signInLoading) {
                    CircularProgressIndicator()
                } else {
                    IconButton(
                        onClick = {
                            /* Googleでログイン */
                            /* ここをエラーの理由をちゃんと吐かせないとだめｄな。 */
                            authViewModel.viewModelScope.launch {
                                val result = CredentialManagerHelper.getGoogleIdToken(context)
                                if (result !is FetchResult.Success) {
                                    val errorMessage = result.toSuspendFuncStatusInfo().errorMessage
                                    snackBarHostState.currentSnackbarData?.dismiss()
                                    snackBarHostState.showSnackbar(
                                        "Googleログインに失敗しました: ${errorMessage}",
                                        duration = SnackbarDuration.Long
                                    )
                                    return@launch
                                }
                                val idToken = result.data
                                authViewModel.signInWithGoogleIdToken(idToken)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        /* 広げないとめっちゃ小さくなる */
                        Image(
                            painter = painterResource(id = R.drawable.android_light_rd),
                            contentDescription = "Google Sign In",
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // 🎯 email・password・Login → 画面の縦中央に固定
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("mail") },
                        singleLine = true,
                        enabled = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("password") },
                        singleLine = true,
                        enabled = true
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    if (!loading) {
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLoginState)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.tertiary
                            ),
                            onClick = {
                                /* 既存のSnackbarを消す */
                                snackBarHostState.currentSnackbarData?.dismiss()
                                // ログイン処理
                                if (isLoginState) {
                                    handleLogin()
                                } else {
                                    handleSignUp()
                                }
                            }
                        ) {
                            Text(if (isLoginState) "Login" else "SignUp")
                        }
                    } else {
                        CircularProgressIndicator()
                    }
                }

                // 🎯 Forgot password を下部に独立配置（中央の配置に影響しない！）
                if (isLoginState && !loading) {
                    Text(
                        text = "Forgot password?",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        ),
                        modifier = Modifier
                            .align(Alignment.Center) // 👈 Login直下に揃えたい場合は Center
                            .padding(top = 300.dp)
                            .clickable {
                                // パスワードリセット処理
                                navController.navigate(
                                    Screen.StartScreen.ForgotPassword.route
                                )
                            }
                    )
                }
            }
        }

    }
}