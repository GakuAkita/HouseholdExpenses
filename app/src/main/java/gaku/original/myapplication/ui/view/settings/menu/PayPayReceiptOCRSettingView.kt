package gaku.original.myapplication.ui.view.settings.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.viewModel.settings.PayPayReceiptOCRSettingViewModel

/**
 * 使わない
 * サンプル画像を使ってもおそらく、携帯の縮尺によって画像も変わる。
 * ここでサンプル画像を使ってやるべきではない。
 */
@Composable
fun PayPayReceiptOCRSettingView(
    navController: NavController,
    viewModel: PayPayReceiptOCRSettingViewModel = hiltViewModel()
) {

    Scaffold(
        topBar = {
            TopBarView(
                title = "PayPayレシートOCR設定",
                onBackNavClicked = {
                    navController.popBackStack()
                },
                showBackButton = true
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            Text("PayPayレシートのマスキング設定を変えたいときは、ここで設定をリセットする。")
            Text("次にPayPayレシート読み取り機能を使ったときに再設定する")
        }
    }
}