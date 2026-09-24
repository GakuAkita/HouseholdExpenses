package gaku.original.myapplication.ui.screens.global.settingMenu.paypayReceiptReader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.ui.common.TopBarView

@Composable
fun PayPayReceiptReaderScreenRoot(
    navHostController: NavHostController,
    viewModel: PayPayReceiptReaderViewModel = viewModel(factory = PayPayReceiptReaderViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackBarHostState.current

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    PayPayReceiptReaderScreen(
        uiState,
        snackbarHostState,
        onBackNavClick = {
            navHostController.popBackStack()
        },
        onResetClick = {
            viewModel.resetSetting()
        }
    )
}

@Composable
fun PayPayReceiptReaderScreen(
    uiState: PayPayReceiptReaderUiState,
    snackbarHostState: SnackbarHostState,
    onBackNavClick: () -> Unit,
    onResetClick: () -> Unit
) {

    Scaffold(
        topBar = {
            TopBarView(
                title = "PayPay Receipt Reader Setting",
                onBackNavClicked = {
                    onBackNavClick()
                },
                showBackButton = true
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
        ) {
            if (uiState.topPercent == null || uiState.leftPercent == null) {
                Text("Masking Setting is not done.")
                Text("When you use PayPay Receipt Reader function, you need to set masking setting.")
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Top Ratio:${uiState.topPercent}")
                        Text("Left Ratio:${uiState.leftPercent}")
                    }
                    Button(
                        onClick = {
                            onResetClick()
                        }
                    ) {
                        Text("Ratio Reset")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PayPayReceiptReaderScreenPreview() {
    val uiState = PayPayReceiptReaderUiState(
        isLoading = false,
        message = null,
        topPercent = 0.1f,
        leftPercent = 0.2f,
        isLoadError = false
    )
    PayPayReceiptReaderScreen(
        uiState,
        SnackbarHostState(),
        onBackNavClick = {},
        onResetClick = {}
    )
}
