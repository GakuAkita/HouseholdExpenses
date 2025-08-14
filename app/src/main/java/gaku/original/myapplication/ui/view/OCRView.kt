package gaku.original.myapplication.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import gaku.original.myapplication.Screen
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.utility.navigateToSingle
import gaku.original.myapplication.viewModel.OCRViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OCRView(
    viewModel: OCRViewModel = hiltViewModel(),
    navController: NavController
) {
    val viewName = "OCRView"
    val context = LocalContext.current
    val ocrResult = viewModel.ocrResult.collectAsState()
    val uriUpdatedTimestamp = viewModel.uriUpdatedTimestamp.collectAsState()

    val extractedExpense = viewModel.extractedExpense.collectAsState()

    var showExtractedExpenseDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uriUpdatedTimestamp.value) {/* .valueつけてなかった、、、道理で更新されないわけだわ */
        viewModel.createExpenseByPayPayReceipt(context) {
            showExtractedExpenseDialog = true
        }
    }

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
                .verticalScroll(rememberScrollState())
        ) {
            Button(
                onClick = {
                    viewModel.createExpenseByPayPayReceipt(context)
                }
            ) {
                Text("再読み込み")
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 3.dp)
            )
            Text(ocrResult.value?.text ?: "")
            HorizontalDivider(modifier = Modifier.padding(vertical = 3.dp))
            AsyncImage(
                model = viewModel.getImageUri(),
                contentDescription = null
            )
        }
    }

    if (showExtractedExpenseDialog) {
        BasicAlertDialog(
            onDismissRequest = {
                showExtractedExpenseDialog = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.onTertiary
                )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("日付: ${extractedExpense.value.datetime}")
                Text("金額: ${extractedExpense.value.amount}")
            }
        }
    }
}

/* OCR画面は基本的に単一画面。すでに存在する場合は強制的に上書きする */
fun navigateToOCRView(navController: NavHostController) {
    navigateToSingle(navController, Screen.GlobalScreen.OcrRead.route)
}

