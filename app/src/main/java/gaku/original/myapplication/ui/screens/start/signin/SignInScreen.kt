package gaku.original.myapplication.ui.screens.start.signin

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.R
import gaku.original.myapplication.ui.common.TopBarView
import kotlinx.coroutines.launch
import timber.log.Timber


@Composable
fun SignInScreenRoot(
    viewModel: SignInViewModel = viewModel(factory = SignInViewModel.Factory),
    isSignIn: Boolean = true,
    isGoogleOnly: Boolean = true,
    onBackNavClick: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = LocalSnackBarHostState.current

    val activity = LocalContext.current as Activity
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Timber.d("Message:$it")
            /* snackbar */
            snackbarHostState.showSnackbar(message = it, actionLabel = "OK")
            viewModel.onMessageShown()
        }
    }

    SignInScreen(
        uiState,
        snackbarHostState,
        isSignIn = isSignIn,
        isGoogleOnly = isGoogleOnly,
        onGoogleClick = {
            scope.launch {
                viewModel.signInWithGoogle(activity)
            }
        },
        onBackNavClick = onBackNavClick,
        onEmailChange = {
            viewModel.onEmailChange(it)
        },
        onPasswordChange = {
            viewModel.onPasswordChange(it)
        },
        onSignInClick = {
            if (isSignIn) {
                viewModel.signInWithEmail()
            } else {
                viewModel.signUpWithEmail()
            }
        },
        onForgotPasswordClick = {

        }
    )
}

@Composable
fun SignInScreen(
    uiState: SignInUiState,
    snackbarHostState: SnackbarHostState,
    isSignIn: Boolean,
    isGoogleOnly: Boolean = true,
    onGoogleClick: () -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignInClick: () -> Unit,
    onBackNavClick: (() -> Unit)? = null,
    onForgotPasswordClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopBarView(
                title = if (isSignIn) "SignIn" else "SignUp",
                showBackButton = onBackNavClick != null,
                onBackNavClicked = {
                    onBackNavClick?.invoke()
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            if (isGoogleOnly) {
                /**
                 * Googleログインだけに絞る場合
                 */
                Text(text = "Googleログインのみにする", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(20.dp))
            }

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                if (uiState.isGoogleEnabled) {
                    Box(
                        modifier = Modifier
                            .clickable {
                                onGoogleClick()
                            }
                            .padding(vertical = 20.dp),
                    ) {
                        /* 広げないとめっちゃ小さくなる */
                        Image(
                            modifier = Modifier.widthIn(max = 200.dp),
                            painter = painterResource(id = R.drawable.android_light_sq_si_4x),
                            contentDescription = "Google Sign In",
                        )
                    }
                }
            }

            if (!isGoogleOnly) {
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
                            containerColor = if (isSignIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                        ),
                        onClick = {
                            onSignInClick()
                        }
                    ) {
                        Text(if (isSignIn) "SignIn" else "SignUp")
                    }
                }
            }

            // 🎯 Forgot password を下部に独立配置（中央の配置に影響しない！）

            if (!uiState.isLoading && isSignIn && !isGoogleOnly) {
                Text(
                    text = "Forgot password?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    ),
                    modifier = Modifier
                        .padding(top = 30.dp)
                        .clickable {
                            onForgotPasswordClick()
                        }
                )
            }
        }
    }
}

@Preview
@Composable
fun SignInScreenPreview() {
    val uiState: SignInUiState = SignInUiState(
        isLoading = false
    )

    SignInScreen(
        uiState = uiState,
        snackbarHostState = SnackbarHostState(),
        isSignIn = true,
        isGoogleOnly = true,
        onGoogleClick = {},
        onEmailChange = {},
        onPasswordChange = {},
        onSignInClick = {
        },
        onBackNavClick = {},
        onForgotPasswordClick = {}
    )
}