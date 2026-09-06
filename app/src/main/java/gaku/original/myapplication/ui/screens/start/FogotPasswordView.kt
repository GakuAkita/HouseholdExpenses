package gaku.original.myapplication.ui.screens.start

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun ForgotPasswordView(
    navController: NavHostController,
) {
//    var email by remember { mutableStateOf("") }
//
//    val scope = rememberCoroutineScope()
//    val snackBarHostState = remember {
//        SnackbarHostState()
//    }
//
//    fun getErrorMsgFromCode(errorCode: String?): String {
//        val message = when (errorCode) {
//            "ERROR_INVALID_EMAIL" -> "メールアドレスの形式が正しくありません。"
////            "ERROR_USER_NOT_FOUND" -> "ユーザーが存在しません。"
//            else -> "パスワード再設定メール送信に失敗しました。"
//        }
//
//        return message
//    }
//
//    Scaffold(
//        topBar = {
//            TopBarView(
//                title = "Forgot Password?",
//                showBackButton = true,
//                onBackNavClicked = { navController.popBackStack() })
//        },
//        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
//    ) { innerPadding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding),
////                .border(2.dp, Color.Red),//デバッグのため,
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            TextField(
//                value = email,
//                onValueChange = { email = it },
//                label = { Text("mail") },
//                singleLine = true
//            )
//
//            Spacer(modifier = Modifier.height(30.dp))
//            Button(
//                onClick = {
//                    viewModel.sendPasswordResetEmailWithCallback(
//                        email = email,
//                        callback = { statusInfo ->
//                            snackBarHostState.currentSnackbarData?.dismiss()
//                            when (statusInfo.status) {
//                                FuncStatus.SUCCESS -> {
//                                    scope.launch {
//                                        snackBarHostState.showSnackbar(
//                                            "パスワード再設定メールを送信しました。",
//                                            actionLabel = "OK",
//                                            duration = SnackbarDuration.Short
//                                        )
//                                    }
//                                }
//
//                                FuncStatus.TIMEOUT -> {
//                                    scope.launch {
//                                        snackBarHostState.showSnackbar(
//                                            "タイムアウトしました。",
//                                            actionLabel = "OK",
//                                            duration = SnackbarDuration.Short
//                                        )
//                                    }
//                                }
//
//                                FuncStatus.FAILED -> {
//                                    scope.launch {
//                                        snackBarHostState.showSnackbar(
//                                            getErrorMsgFromCode(statusInfo.errorCode),
//                                            actionLabel = "OK",
//                                            duration = SnackbarDuration.Short
//                                        )
//                                    }
//                                }
//
//                                FuncStatus.WARNING -> {
//
//                                }
//                            }
//
//                        }
//                    )
//                }
//            ) {
//                Text("パスワード再設定メール送信")
//            }
//        }
//    }
}