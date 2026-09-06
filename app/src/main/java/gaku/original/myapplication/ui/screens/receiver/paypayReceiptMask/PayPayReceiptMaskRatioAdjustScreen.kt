package gaku.original.myapplication.ui.screens.receiver.paypayReceiptMask

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.ui.common.TopBarView

@Composable
fun PayPayReceiptMaskRatioAdjustScreenRoot(
    navHostController: NavHostController,
    viewModel: PayPayReceiptMaskRatioAdjustViewModel = viewModel(factory = PayPayReceiptMaskRatioAdjustViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    PayPayReceiptMaskRatioAdjustScreen(
        uiState,
        onBackNavClick = {
            navHostController.popBackStack()
        }
    )
}

@Composable
fun PayPayReceiptMaskRatioAdjustScreen(
    uiState: PayPayReceiptMaskRatioAdjustUiState,
    onBackNavClick: () -> Unit
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
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            Text("Adjust the masking ratio")
            Text("Left masking ratio:%.4f".format(uiState.leftRatio))
            Text("Top masking ratio:%.4f".format(uiState.topRatio))
        }
    }
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