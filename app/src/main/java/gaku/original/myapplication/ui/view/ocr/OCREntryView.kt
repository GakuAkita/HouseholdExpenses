package gaku.original.myapplication.ui.view.ocr

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.Screen
import gaku.original.myapplication.data.Constants.AppPackageNames
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.utility.navigateAndRemoveCurrent
import gaku.original.myapplication.utility.navigateToSingle
import gaku.original.myapplication.viewModel.ocr.OCREntryViewModel

/**
 * ここで次に行くViewの分岐をする。
 */
@Composable
fun OCREntryView(
    navController: NavHostController,
    viewModel: OCREntryViewModel = hiltViewModel()
) {

    LaunchedEffect(Unit) {
        if (viewModel.sharedImageData.value?.packageName == AppPackageNames.PAYPAY) {
            if (viewModel.isPayPayReceiptTopRatioSet.value && viewModel.isPayPayReceiptLeftRatioSet.value) {
                /**
                 * 両方すでにセットされているのでOCR読み取りへ直行
                 */
            } else {
                /**
                 * Ratio設定画面へ
                 */
                navigateAndRemoveCurrent(
                    navController,
                    Screen.GlobalScreen.OCR.MaskRatioAdjust.route
                )
            }
        } else {
            /**
             * 対応していない画像
             * スクリーンを閉じてToastを出す
             */
        }
    }

    Scaffold(
        topBar = {
            TopBarView(
                title = "OCR 遷移中..",
                onBackNavClicked = {
                    navController.popBackStack()
                },
            )
        }
    ) { innerPadding ->
        /**
         * 将来的にPayPay以外からも画像共有することになった場合、
         * ここでSharedViewModelのパッケージ名を見て遷移する画面を決める
         */
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            CircularProgressIndicator()
        }
    }
}

/* OCR画面は基本的に単一画面。すでに存在する場合は強制的に上書きする */
fun navigateToOCREntryView(navController: NavHostController) {
    navigateToSingle(navController, Screen.GlobalScreen.OCR.Entry.route)
}