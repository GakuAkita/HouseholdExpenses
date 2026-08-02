package gaku.original.myapplication.ui.view.ocr

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.Screen
import gaku.original.myapplication.data.AppTimeZone
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.utility.LogAkitaDebug
import gaku.original.myapplication.viewModel.ocr.OCRViewModel
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OCRView(
    viewModel: OCRViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val viewName = "OCRView"
    val context = LocalContext.current
    val ocrResult by viewModel.ocrResult.collectAsState()
    val extractedExpense by viewModel.extractedExpense.collectAsState()
    val ocrReading by viewModel.ocrReading.collectAsState()
    val sharedImageData by viewModel.sharedImageData.collectAsState()
    val maskedBitmap by viewModel.maskedBitmap.collectAsState()

    /* これ初期値だけでいいか、、 */
    val isLeftRatioSet by viewModel.isLeftRatioSet.collectAsState()
    val isTopRatioSet by viewModel.isTopRatioSet.collectAsState()

    var showExtractedExpenseDialog by rememberSaveable { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackBarHostState = remember {
        SnackbarHostState()
    }

    LaunchedEffect(sharedImageData) {/* .valueつけてなかった、、、道理で更新されないわけだわ */
        viewModel.runOcr(context) {
            if (it.status == FuncStatus.SUCCESS) {
                /**
                 * tmpExpenseにexpenseをコピーする
                 */
                Toast.makeText(context, "OCR読み取りに成功しました", Toast.LENGTH_SHORT).show()
                viewModel.copyReadExpenseToTmpExpense()
                viewModel.clearSharedImageData()
                /**
                 * ↓これも仮数で置き換える？
                 */
                // @TODO 作るなおし
//                navController.navigate(Screen.
//                GlobalScreen.ExpenseAddEdit.route) {
//                    popUpTo(Screen.GlobalScreen.OCR.Read.route) {/* OCR画面をスタックから消してnavigate。そうじゃないと戻ったときにまたOCRが走ってしまう*/
//                        inclusive = true // OCRView を含めて削除
//                    }
//                }
            } else if (it.status == FuncStatus.WARNING) {
                Toast.makeText(context, "OCR読み取り部分的に失敗しました", Toast.LENGTH_SHORT)
                    .show()
                showExtractedExpenseDialog = true
            } else {
                scope.launch {
                    snackBarHostState.currentSnackbarData?.dismiss()
                    snackBarHostState.showSnackbar(
                        "OCR読み取りに失敗しました:${it.errorMessage}",
                        actionLabel = "OK"
                    )
                }
            }
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
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            if (false) {
                LogAkitaDebug("aaa")
            }
            /**
             * 初回読み取り時、または、SharedPreferencesに設定値が残っていないとき
             * ここで調整して設定できるようにする
             */

            if (ocrReading) {
                CircularProgressIndicator()
            } else {
                Column {
                    Button(
                        onClick = {
                            viewModel.runOcr(context) {
                                /**
                                 * 再読み込みの場合は毎回ユーザーに確認をとる
                                 */
                                showExtractedExpenseDialog = true
                            }
                        }
                    ) {
                        Text("再読み込み")
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                    Text(ocrResult?.text?.text ?: "")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 3.dp))
                    if (maskedBitmap != null) {
                        /* maskedBitmapがnullでないときのみ表示 */
                        Image(
                            bitmap = maskedBitmap!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
//                    AsyncImage(
//                        model = viewModel.getImageUri()
//                            .toString() + "?ts=${sharedImageData.value?.receivedTime}",
//                        contentDescription = null
//                    )
                }
            }
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
                val timeZone = AppTimeZone.isoStringToLocalDateTime(extractedExpense.datetime)
                Text("日付: ${timeZone?.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))}")
                Text("金額: ${extractedExpense.amount}")
                Text("店名: ${extractedExpense.storeName}")

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {

                        }
                    ) {
                        Text("はい")
                    }
                }
            }
        }
    }
}

