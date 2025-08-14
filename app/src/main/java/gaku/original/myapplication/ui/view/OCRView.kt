package gaku.original.myapplication.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import gaku.original.myapplication.Screen
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.utility.navigateToSingle
import gaku.original.myapplication.viewModel.OCRViewModel

@Composable
fun OCRView(
    viewModel: OCRViewModel = hiltViewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    val ocrResult = viewModel.ocrResult.collectAsState()


    Scaffold(
        topBar = {
            TopBarView(
                title = "OCR 読み取り",
                onBackNavClicked = {
                    navController.popBackStack()
                },
                showBackButton = true
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Button(
                onClick = {
                    viewModel.runOcr(
                        context
                    )
                }
            ) {
                Text("リード")
            }
            Text("OCR結果：${ocrResult.value}")
            Text("読み込んだ画像")
            AsyncImage(
                model = viewModel.getImageUri(),
                contentDescription = null
            )
        }
    }
}

/* OCR画面は基本的に単一画面。すでに存在する場合は強制的に上書きする */
fun navigateToOCRView(navController: NavHostController) {
    navigateToSingle(navController, Screen.GlobalScreen.OcrRead.route)
}