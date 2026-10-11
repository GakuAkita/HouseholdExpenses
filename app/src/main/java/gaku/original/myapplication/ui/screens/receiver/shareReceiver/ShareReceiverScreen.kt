package gaku.original.myapplication.ui.screens.receiver.shareReceiver

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import gaku.original.myapplication.MainActivity
import gaku.original.myapplication.SharedReceiverGraph
import gaku.original.myapplication.data.Constants.ShareIntentKeys
import gaku.original.myapplication.ui.common.LoadingBox
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.ui.screens.receiver.paypayReceiptMask.SentDataExpenseDisplay
import timber.log.Timber

@Composable
fun ShareReceiverScreenRoot(
    viewModel: ShareReceiverViewModel,
    navHostController: NavHostController,
    onComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    LaunchedEffect(uiState.notMaskSet) {
        if (uiState.notMaskSet) {
            Timber.d("Launch MaskAdjust screen")
            navHostController.navigate(
                SharedReceiverGraph.SharedReceiver.PayPayReceiptMaskRatioAdjust(
                    uiState.imagePath!!
                )
            )
            viewModel.onNotMaskSetDone()// lower notMaskSet, clear imagePath
        }
    }

    LaunchedEffect(uiState.sentData) {
        when (val sentData = uiState.sentData) {
            is SentData.Expense -> {
                if (sentData.datetime != null &&
                    sentData.amount != null &&
                    sentData.storeName != null
                ) {/* startActivity */
                    startMainActivity(context, sentData)
                    onComplete()
                }
            }

            null -> {/* Do nothing */
            }
        }
    }

    ShareReceiverScreen(
        uiState,
        snackbarHostState,
        onAddExpenseClick = {
            startMainActivity(context, it)
        },
        onAnalyzeClick = {
            viewModel.onAnalyzeClick()
        }
    )
}

@Composable
fun ShareReceiverScreen(
    uiState: ShareReceiverUiState,
    snackbarHostState: SnackbarHostState,
    onAddExpenseClick: (SentData.Expense) -> Unit,
    onAnalyzeClick: () -> Unit
) {

    Scaffold(topBar = {
        TopBarView(
            title = "Received Data",
        )
    }, snackbarHost = {
        SnackbarHost(snackbarHostState)
    }) { innerPadding ->
        if (uiState.isLoading) {
            LoadingBox(modifier = Modifier.padding(innerPadding))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val sentData = uiState.sentData) {
                is SentData.Expense -> {
                    SentDataExpenseDisplay(data = sentData)

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        onClick = {
                            onAddExpenseClick(sentData)
                        }
                    ) {
                        Text("Add to Expense")
                    }
                }

                null -> {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        onClick = {
                            onAnalyzeClick()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DocumentScanner,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyze the image")
                    }
                }
            }

            uiState.bitmap?.let { bitmap ->
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "masked image",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun ShareReceiverScreenPreview() {
    val uiState = ShareReceiverUiState(
        isLoading = false,
//        sentData = SentData.Expense(
//            datetime = LocalDateTime.now().toIsoUtcString(ZoneId.systemDefault()),
//            amount = 1000,
//            storeName = null
//        )
        sentData = null
    )
    ShareReceiverScreen(
        uiState,
        SnackbarHostState(),
        onAddExpenseClick = {},
        onAnalyzeClick = {}
    )
}

fun startMainActivity(context: Context, sentData: SentData) {
    val mainIntent = Intent(context, MainActivity::class.java).apply {
        putExtra(ShareIntentKeys.EXPENSE, sentData)
        //flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    context.startActivity(
        mainIntent
    )
}