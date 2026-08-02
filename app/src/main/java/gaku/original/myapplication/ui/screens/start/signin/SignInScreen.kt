package gaku.original.myapplication.ui.screens.start.signin

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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.R
import gaku.original.myapplication.ui.common.TopBarView
import timber.log.Timber


@Composable
fun SignInScreenRoot(
    viewModel: SignInViewModel = viewModel(factory = SignInViewModel.Factory),
    isSignIn: Boolean = true,
    isGoogleOnly: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = LocalSnackBarHostState.current

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Timber.d("Message:$it")
            /* snackbar */
        }
    }

    SignInScreen(
        uiState,
        snackbarHostState,
        isSignIn = isSignIn,
        googleOnly = isGoogleOnly,
        onGoogleClick = {},
        onBackNavClick = {},
        onEmailChange = {

        },
        onPasswordChange = {
        },
        onForgotPasswordClick = {}
    )
}

@Composable
fun SignInScreen(
    uiState: SignInUiState,
    snackbarHostState: SnackbarHostState,
    isSignIn: Boolean,
    googleOnly: Boolean = true,
    onGoogleClick: () -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onBackNavClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {

//    fun getSignInErrorMsgFromCode(errorCode: String?): String {
//        val message = when (errorCode) {
//            "ERROR_INVALID_EMAIL" -> "メールアドレスの形式が正しくありません。"
//            "ERROR_USER_DISABLED" -> "このアカウントは無効です。"
//            "ERROR_USER_NOT_FOUND" -> "ユーザーが存在しません。"
//            "ERROR_WRONG_PASSWORD" -> "パスワードが間違っています。"
//            "_EMAIL_NOT_VERIFIED" -> "メールアドレスが認証されていません。認証メールを再送します。"
//            else -> "ログインに失敗しました。"
//        }
//        return message
//    }
//
//    fun getSignUpErrorMsgFromCode(errorCode: String?): String {
//        val message = when (errorCode) {
//            "ERROR_INVALID_EMAIL" -> "メールアドレスの形式が正しくありません。"
//            "ERROR_WEAK_PASSWORD" -> "パスワードが弱すぎます。6文字以上にしてください。"
//            "ERROR_OPERATION_NOT_ALLOWED" -> "この操作は許可されていません。"
//            else -> "アカウント作成に失敗しました"
//        }
//        return message
//    }

//    fun handleLogin() {
//        authViewModel.signInWithCallback(
//            email = email,
//            password = password,
//            callback = { status ->
//                when (status.status) {
//                    FuncStatus.SUCCESS -> {
//                        scope.launch {
//                            snackBarHostState.showSnackbar(
//                                "ログインしました",
//                                actionLabel = "OK",
//                                duration = SnackbarDuration.Long
//                            )
//                        }
////                        navController.navigate(Screen.MainScreen.Content.route) {
////                            //ログイン画面をスタックから削除して、MainScreen.Contentが一番上に来るように。
////                            popUpTo(0) {
////                                inclusive = true
////                            }
////                        }
//                    }
//
//                    FuncStatus.TIMEOUT -> {
//                        scope.launch {
//                            snackBarHostState.showSnackbar(
//                                "ログインできずタイムアウトしました",
//                                actionLabel = "OK",
//                                duration = SnackbarDuration.Long
//                            )
//                        }
//                        loading = false
//                    }
//
//                    FuncStatus.FAILED -> {
//                        val errorMsg =
//                            getSignInErrorMsgFromCode(status.errorCode)
//                        LogAkitaDebug(errorMsg)
//                        scope.launch {
//                            snackBarHostState.showSnackbar(
//                                errorMsg,
//                                actionLabel = "OK",
//                                duration = SnackbarDuration.Long
//                            )
//                        }
//                        loading = false
//                    }
//
//                    FuncStatus.WARNING -> {
//
//                    }
//                }
//            }
//        )
//    }
//
//    fun handleSignUp() {
//        authViewModel.signUpWithCallback(
//            email,
//            password,
//            callback = { status ->
//                when (status.status) {
//                    FuncStatus.SUCCESS -> {
//                        scope.launch {
//                            snackBarHostState.showSnackbar(
//                                "アカウントを作成しました。メールアドレス認証をしてください。\n認証後、ログインボタンを押してください",
//                                actionLabel = "OK",
//                            )
//                            //サインアップに成功したら画面がログイン画面に切り替わる
//                            //※画面のUIが変わっているだけでルートは変わっていない!!
//                        }
//                        loading = false
//                        isLoginState = true
//                    }
//
//                    FuncStatus.TIMEOUT -> {
//                        scope.launch {
//                            snackBarHostState.showSnackbar(
//                                "タイムアウトしました。アカウント作成に失敗しました",
//                                actionLabel = "OK",
//                                duration = SnackbarDuration.Long
//                            )
//                        }
//                        loading = false
//                    }
//
//                    FuncStatus.FAILED -> {
//                        scope.launch {
//                            val errorMsg =
//                                getSignUpErrorMsgFromCode(status.errorCode)
//                            LogAkitaDebug(errorMsg)
//                            snackBarHostState.showSnackbar(
//                                errorMsg,
//                                actionLabel = "OK",
//                                duration = SnackbarDuration.Long
//                            )
//                        }
//                        loading = false
//                    }
//
//                    FuncStatus.WARNING -> {
//
//                    }
//                }
//            }
//        )
//    }

//    val user by authViewModel.currentUser.collectAsState()
//    val signInLoading by authViewModel.signInLoading.collectAsState()
//    LaunchedEffect(user) {
//        /**
//         * userが変化したら走る。
//         */
//        if (user != null) {
////            scope.launch {
////                snackBarHostState.showSnackbar(
////                    "ログインしました。",
////                    actionLabel = "OK",
////                    duration = SnackbarDuration.Long
////                )
////            }
//            /* Mainスクリーンに遷移 */
////            navController.navigate(Screen.MainScreen.Content.route) {
////                popUpTo(0) { inclusive = true }
////            }
//        }
//    }

    Scaffold(
        topBar = {
            TopBarView(
                title = if (isSignIn) "SignIn" else "SignUp",
                showBackButton = true,
                onBackNavClicked = {
                    onBackNavClick()
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
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
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    IconButton(
                        onClick = {
                            /* Googleでログイン */
                            /* ここをエラーの理由をちゃんと吐かせないとだめｄな。 */
//                            authViewModel.viewModelScope.launch {
//                                val result = CredentialManagerHelper.getGoogleIdToken(context)
//                                if (result !is FuncResultWithData.Success) {
//                                    val errorMessage = result.toFuncStatusInfo().errorMessage
//                                    snackBarHostState.currentSnackbarData?.dismiss()
//                                    snackBarHostState.showSnackbar(
//                                        "Googleログインに失敗しました: ${errorMessage}",
//                                        duration = SnackbarDuration.Long
//                                    )
//                                    return@launch
//                                }
//                                val idToken = result.data
//                                authViewModel.signInWithGoogleIdToken(idToken)
//                            }
                            onGoogleClick()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        /* 広げないとめっちゃ小さくなる */
                        Image(
                            painter = painterResource(id = R.drawable.android_light_sq_si_4x),
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
                        value = uiState.email,
                        onValueChange = {
                            onEmailChange(it)
                        },
                        label = { Text("mail") },
                        singleLine = true,
                        enabled = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TextField(
                        value = uiState.password,
                        onValueChange = {
                            onPasswordChange(it)
                        },
                        label = { Text("password") },
                        singleLine = true,
                        enabled = true
                    )
                    Spacer(modifier = Modifier.height(30.dp))

                    if (uiState.isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                            ),
                            onClick = {

                            }
                        ) {
                            Text(if (isSignIn) "SignIn" else "SignUp")
                        }
                    }
                }

                // 🎯 Forgot password を下部に独立配置（中央の配置に影響しない！）
                if(!uiState.isLoading && isSignIn){
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
                                onForgotPasswordClick()
                            }
                    )
                }
            }
        }

    }
}