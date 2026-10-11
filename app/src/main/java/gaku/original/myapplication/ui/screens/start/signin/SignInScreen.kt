package gaku.original.myapplication.ui.screens.start.signin

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
                title = if (isSignIn) "Sign In" else "Sign Up",
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
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isSignIn) "Welcome back" else "Create your account",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isGoogleOnly) "Sign in with your Google account to continue."
                else "Use your Google account or email address.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                if (uiState.isGoogleEnabled) {
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.extraLarge)
                            .clickable {
                                onGoogleClick()
                            }
                            .padding(4.dp),
                    ) {
                        /* 広げないとめっちゃ小さくなる */
                        Image(
                            modifier = Modifier.widthIn(max = 220.dp),
                            painter = painterResource(id = R.drawable.android_light_sq_si_4x),
                            contentDescription = "Google Sign In",
                        )
                    }
                }
            }

            if (!isGoogleOnly) {
                Row(
                    modifier = Modifier
                        .widthIn(max = 400.dp)
                        .padding(vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = "or",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                OutlinedTextField(
                    modifier = Modifier
                        .widthIn(max = 400.dp)
                        .fillMaxWidth(),
                    value = uiState.email,
                    onValueChange = {
                        onEmailChange(it)
                    },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    enabled = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    modifier = Modifier
                        .widthIn(max = 400.dp)
                        .fillMaxWidth(),
                    value = uiState.password,
                    onValueChange = {
                        onPasswordChange(it)
                    },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    enabled = true
                )
                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        modifier = Modifier
                            .widthIn(max = 400.dp)
                            .fillMaxWidth()
                            .height(48.dp),
                        onClick = {
                            onSignInClick()
                        }
                    ) {
                        Text(if (isSignIn) "Sign In" else "Sign Up")
                    }
                }
            }

            if (!uiState.isLoading && isSignIn && !isGoogleOnly) {
                TextButton(
                    modifier = Modifier.padding(top = 16.dp),
                    onClick = {
                        onForgotPasswordClick()
                    }
                ) {
                    Text("Forgot password?")
                }
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