package gaku.original.myapplication.ui.view.ocr

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import gaku.original.myapplication.ui.common.TopBarView

/**
 * ここで次に行くViewの分岐をする。
 */
@Composable
fun OCREntryView(
    navController: NavController,
    viewModel:
){
    Scaffold(
        topBar = {
            TopBarView(
                title = "OCR 遷移中..",
                onBackNavClicked = {
                    navController.popBackStack()
                },
            )
        }
    ) {innerPadding->
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