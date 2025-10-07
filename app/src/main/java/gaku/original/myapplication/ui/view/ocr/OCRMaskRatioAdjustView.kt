package gaku.original.myapplication.ui.view.ocr

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.utility.LogAkitaDebug
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
            modifier = Modifier.padding(innerPadding)
        ) {
            Text("マスキングの割合を調整します")
            Text("左マスキング割合:${viewModel.leftRatio.value}")
            Text("上マスキング割合:${viewModel.topRatio.value}")


            Slider(
                value = viewModel.leftRatio.value,
                onValueChange = {
                    viewModel.updateLeftRatio(it)
                }
            )

            Slider(
                value = viewModel.topRatio.value,
                onValueChange = {
                    viewModel.updateTopRatio(it)
                }
            )

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