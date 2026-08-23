package gaku.original.myapplication.ui.screens.global.settingMenu.paypayReceiptReader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.ui.common.TopBarView

@Composable
fun PayPayReceiptReaderScreenRoot(
    navHostController: NavHostController,
    viewModel: PayPayReceiptReaderViewModel = viewModel(factory = PayPayReceiptReaderViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
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
            if (uiState.topRatio == null || uiState.leftRatio == null) {
                Text("Masking Setting is not done.")
                Text("When you use PayPay Receipt Reader function, you need to set masking setting.")
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
        topRatio = 0.1f,
        leftRatio = 0.2f,
        isLoadError = false
    )
    PayPayReceiptReaderScreen(
        uiState,
        SnackbarHostState(),
        onBackNavClick = {},
        onResetClick = {}
    )
}


/**
 * 使わない
 * サンプル画像を使ってもおそらく、携帯の縮尺によって画像も変わる。
 * ここでサンプル画像を使ってやるべきではない。
 */
@Composable
fun PayPayReceiptOCRSettingView(
    navController: NavController,
    viewModel: PayPayReceiptReaderViewModel = hiltViewModel()
) {

//    Scaffold(
//        topBar = {
//            TopBarView(
//                title = "PayPayレシートOCR設定",
//                onBackNavClicked = {
//                    navController.popBackStack()
//                },
//                showBackButton = true
//            )
//        }
//    ) { innerPadding ->
//        Column(
//            modifier = Modifier.padding(innerPadding)
//        ) {
//            Text("PayPayレシートのマスキング設定を変えたいときは、ここで設定をリセットする。")
//            Text("次にPayPayレシート読み取り機能を使ったときに再設定する")
//            HorizontalDivider()
//            if (viewModel.checkBothRatioSet()) {
//                /* 両方セットされているならリセットボタンを押せるように */
//                Text("leftRatio:${viewModel.leftRatio.value}")
//                Text("topRatio:${viewModel.topRatio.value}")
//                Button(onClick = {
//                    viewModel.resetRatio()
//                    navController.popBackStack()
//                }) {
//                    Text("リセット")
//                }
//            } else {
//                Text("まだマスキング設定を変えていないのでPayPayレシート読み取り機能を使用したときにマスキング設定画面が開きます")
//            }
//        }
//    }
}