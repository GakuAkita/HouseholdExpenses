package gaku.original.myapplication.ui.screens.receiver

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import gaku.original.myapplication.MainActivity
import gaku.original.myapplication.data.Constants.ShareIntentKeys
import gaku.original.myapplication.data.repository.appTimeZone.toIsoUtcString
import gaku.original.myapplication.ui.common.TopBarView
import java.time.LocalDateTime
import java.time.ZoneId

@Composable
fun ShareReceiverScreenRoot(
    viewModel: ShareReceiverViewModel, navHostController: NavHostController,
    onComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
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
        }
    )
}

@Composable
fun ShareReceiverScreen(
    uiState: ShareReceiverUiState,
    snackbarHostState: SnackbarHostState,
    onAddExpenseClick: (SentData.Expense) -> Unit
) {

    Scaffold(topBar = {
        TopBarView(
            title = "Received Data",
        )
    }, snackbarHost = {
        SnackbarHost(snackbarHostState)
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                uiState.sentData?.let {
                    when (it) {
                        is SentData.Expense -> {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (it.datetime == null) {
                                        Text("Unable to get datetime")
                                    } else {
                                        Text("DateTime:${it.datetime}")
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (it.amount == null) {
                                        Text("Unable to get amount.")
                                    } else {
                                        Text("amount = ${it.amount}")
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (it.storeName == null) {
                                        Text("Unable to get store name.")
                                    } else {
                                        Text("storeName = ${it.storeName}")
                                    }
                                }

                                Button(
                                    onClick = {
                                        onAddExpenseClick(it)
                                    }
                                ) {
                                    Text("Add to Expense")
                                }
                            }
                        }
                    }
                }
                uiState.bitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "masked image"
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
        sentData = SentData.Expense(
            datetime = LocalDateTime.now().toIsoUtcString(ZoneId.systemDefault()),
            amount = 1000,
            storeName = null
        )
    )
    ShareReceiverScreen(
        uiState,
        SnackbarHostState(),
        onAddExpenseClick = {}
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