package gaku.original.myapplication.ui.screens.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
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
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.viewModel.start.ForgotPasswordViewModel
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordView(
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
    navController: NavHostController,
) {
    var email by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val snackBarHostState = remember {
        SnackbarHostState()
    }

    fun getErrorMsgFromCode(errorCode: String?): String {
        val message = when (errorCode) {
            "ERROR_INVALID_EMAIL" -> "メールアドレスの形式が正しくありません。"
//            "ERROR_USER_NOT_FOUND" -> "ユーザーが存在しません。"
            else -> "パスワード再設定メール送信に失敗しました。"
        }

        return message
    }

    Scaffold(
        topBar = {
            TopBarView(
                title = "Forgot Password?",
                showBackButton = true,
                onBackNavClicked = { navController.popBackStack() })
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
//                .border(2.dp, Color.Red),//デバッグのため,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("mail") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = {
                    viewModel.sendPasswordResetEmailWithCallback(
                        email = email,
                        callback = { statusInfo ->
                            snackBarHostState.currentSnackbarData?.dismiss()
                            when (statusInfo.status) {
                                FuncStatus.SUCCESS -> {
                                    scope.launch {
                                        snackBarHostState.showSnackbar(
                                            "パスワード再設定メールを送信しました。",
                                            actionLabel = "OK",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }

                                FuncStatus.TIMEOUT -> {
                                    scope.launch {
                                        snackBarHostState.showSnackbar(
                                            "タイムアウトしました。",
                                            actionLabel = "OK",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }

                                FuncStatus.FAILED -> {
                                    scope.launch {
                                        snackBarHostState.showSnackbar(
                                            getErrorMsgFromCode(statusInfo.errorCode),
                                            actionLabel = "OK",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }

                                FuncStatus.WARNING -> {

                                }
                            }

                        }
                    )
                }
            ) {
                Text("パスワード再設定メール送信")
            }
        }
    }
}