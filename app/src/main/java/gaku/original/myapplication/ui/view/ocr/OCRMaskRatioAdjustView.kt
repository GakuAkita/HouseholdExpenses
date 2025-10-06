package gaku.original.myapplication.ui.view.ocr

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.viewModel.ocr.OCRMaskRatioAdjustViewModel

@Composable
fun OCRMaskRatioAdjustView(
    navController: NavHostController,
    viewModel: OCRMaskRatioAdjustViewModel = hiltViewModel()
) {
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
        }
    }
}