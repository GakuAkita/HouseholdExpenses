package gaku.original.myapplication.ui.screens.receiver.paypayReceiptMask

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.ui.common.TopBarView

@Composable
fun PayPayReceiptMaskRatioAdjustScreenRoot(
    navHostController: NavHostController,
    viewModel: PayPayReceiptMaskRatioAdjustViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = LocalSnackBarHostState.current

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    PayPayReceiptMaskRatioAdjustScreen(
        uiState,
        snackbarHostState = snackbarHostState,
        onBackNavClick = {
            navHostController.popBackStack()
        },
        onLeftRatioPercentChange = {
            //viewModel.updateLeftRatio(it)
        },
        onTopRatioPercentChange = {
            //viewModel.updateTopRatio(it)
        }
    )
}

@Composable
fun PayPayReceiptMaskRatioAdjustScreen(
    uiState: PayPayReceiptMaskRatioAdjustUiState,
    snackbarHostState: SnackbarHostState,
    onBackNavClick: () -> Unit,
    onLeftRatioPercentChange: (Float) -> Unit,
    onTopRatioPercentChange: (Float) -> Unit
) {
    Scaffold(
        topBar = {
            TopBarView(
                title = "PayPay Receipt Masking Setting",
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
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = "Please adjust the top and left masking ratio. Hide the top-left logo in the receipt"
                )
            }

            Column(
                modifier = Modifier.padding(4.dp)
            ) {
                Text("Left masking ratio:%.2f".format(uiState.leftRatio * 100) + "[%]")
                Slider(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .padding(8.dp),
                    value = uiState.leftRatio,
                    onValueChange = {
                        onLeftRatioPercentChange(it)
                    }
                )
            }

            Column(
                modifier = Modifier.padding(4.dp)
            ) {
                Text("Top masking ratio:%.4f".format(uiState.topRatio) + "[%]")
                Slider(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .padding(8.dp),
                    value = uiState.topRatio,
                    onValueChange = {
                        onTopRatioPercentChange(it)
                    }
                )
            }

            if (uiState.bitmap != null) {
                Image(
                    bitmap = uiState.bitmap.asImageBitmap(),
                    contentDescription = "maskedBitmap",
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text("Unable to load image..")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PayPayReceiptMaskRatioAdjustScreenPreview() {
    val uiState = PayPayReceiptMaskRatioAdjustUiState(
        isLoading = false,
        message = null,
        leftRatio = 0.1f,
        topRatio = 0.2f,
        bitmap = null
    )
    PayPayReceiptMaskRatioAdjustScreen(
        uiState,
        snackbarHostState = SnackbarHostState(),
        onBackNavClick = {},
        onLeftRatioPercentChange = {},
        onTopRatioPercentChange = {}
    )
}

//@Composable
//fun PayPayReceiptMaskRatioAdjustScreen(
//    navController: NavHostController,
//) {
//    val context = LocalContext.current
//
//    val sharedImageData by viewModel.sharedImageData.collectAsState()
//    val bitmapShown by viewModel.bitmapShown.collectAsState()
//
//    LaunchedEffect(sharedImageData) {
//        LogAkitaDebug("sharedImageData changed")
//        viewModel.setBitmapShown(context)
//    }
//
//    LaunchedEffect(viewModel.topRatio.value, viewModel.leftRatio.value) {
//        viewModel.setBitmapShown(context)
//    }
//
//    Scaffold(
//        topBar = {
//            TopBarView(
//                title = "OCR マスキング割合調整",
//                onBackNavClicked = {
//                    navController.popBackStack()
//                },
//                showBackButton = true
//            )
//        },
//    ) { innerPadding ->
//        Column(
//            modifier = Modifier
//                .padding(innerPadding)
//                .verticalScroll(rememberScrollState())
//        ) {
//            Text("マスキングの割合を調整します")
//            Text("左マスキング割合:%.4f".format(viewModel.leftRatio.value))
//            Text("上マスキング割合:%.4f".format(viewModel.topRatio.value))
//
//            Text("左マスキングスライドバー")
//            Slider(
//                value = viewModel.leftRatio.value,
//                onValueChange = {
//                    viewModel.updateLeftRatio(it)
//                },
//                onValueChangeFinished = {
//                    //viewModel.setBitmapShown(context)
//                }
//            )
//
//            Text("上マスキングスライドバー")
//            Slider(
//                value = viewModel.topRatio.value,
//                onValueChange = {
//                    viewModel.updateTopRatio(it)
//                },
//                onValueChangeFinished = {
//
//                }
//            )
//            Button(
//                onClick = {
//                    /* 設定値を保存 */
//                    viewModel.saveAdjustedRatioSetting()
//                    //navigateAndRemoveCurrent(navController, Screen.GlobalScreen.OCR.Read.route)
//                }
//            ) {
//                Text("設定値を保存")
//            }
//            Text("設定画面からここで保存したものをリセット&再設定できます。")
//
//            bitmapShown?.let {
//                Image(
//                    bitmap = it.asImageBitmap(),
//                    contentDescription = "maskedBitmap",
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
//        }
//    }
//}