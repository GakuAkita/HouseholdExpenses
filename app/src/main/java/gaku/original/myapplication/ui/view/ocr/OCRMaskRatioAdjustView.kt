package gaku.original.myapplication.ui.view.ocr

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.Screen
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.utility.LogAkitaDebug
import gaku.original.myapplication.utility.navigateAndRemoveCurrent
import gaku.original.myapplication.viewModel.ocr.OCRMaskRatioAdjustViewModel

@Composable
fun OCRMaskRatioAdjustView(
    navController: NavHostController,
    viewModel: OCRMaskRatioAdjustViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val sharedImageData by viewModel.sharedImageData.collectAsState()
    val bitmapShown by viewModel.bitmapShown.collectAsState()

    LaunchedEffect(sharedImageData) {
        LogAkitaDebug("sharedImageData changed")
        viewModel.setBitmapShown(context)
    }

    LaunchedEffect(viewModel.topRatio.value, viewModel.leftRatio.value) {
        viewModel.setBitmapShown(context)
    }

    Scaffold(
        topBar = {
            TopBarView(
                title = "OCR マスキング割合調整",
                onBackNavClicked = {
                    navController.popBackStack()
                },
                showBackButton = true
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Text("マスキングの割合を調整します")
            Text("左マスキング割合:%.4f".format(viewModel.leftRatio.value))
            Text("上マスキング割合:%.4f".format(viewModel.topRatio.value))

            Text("左マスキングスライドバー")
            Slider(
                value = viewModel.leftRatio.value,
                onValueChange = {
                    viewModel.updateLeftRatio(it)
                },
                onValueChangeFinished = {
                    //viewModel.setBitmapShown(context)
                }
            )

            Text("上マスキングスライドバー")
            Slider(
                value = viewModel.topRatio.value,
                onValueChange = {
                    viewModel.updateTopRatio(it)
                },
                onValueChangeFinished = {

                }
            )
            Button(
                onClick = {
                    /* 設定値を保存 */
                    viewModel.saveAdjustedRatioSetting()
                    //navigateAndRemoveCurrent(navController, Screen.GlobalScreen.OCR.Read.route)
                }
            ) {
                Text("設定値を保存")
            }
            Text("設定画面からここで保存したものをリセット&再設定できます。")

            bitmapShown?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "maskedBitmap",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}