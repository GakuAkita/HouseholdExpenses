package gaku.original.myapplication.ui.screens.receiver.paypayReceiptMask

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.data.extractor.ExtractedData
import gaku.original.myapplication.data.repository.appTimeZone.toIsoUtcString
import gaku.original.myapplication.data.repository.appTimeZone.toLocalDateTime
import gaku.original.myapplication.ui.common.ConfirmAlertDialog
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.ui.screens.receiver.shareReceiver.SentData
import java.time.LocalDateTime
import java.time.ZoneId

@Composable
fun PayPayReceiptMaskRatioAdjustScreenRoot(
    navHostController: NavHostController, viewModel: PayPayReceiptMaskRatioAdjustViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = LocalSnackBarHostState.current

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    PayPayReceiptMaskRatioAdjustScreen(
        uiState,
        snackbarHostState = snackbarHostState,
        onFABClick = {
            viewModel.onFABClick()
        },
        onLeftRatioPercentChange = {
            viewModel.onLeftRatioChane(it)
        },
        onTopRatioPercentChange = {
            viewModel.onTopRatioChange(it)
        },
        onSaveClick = {},
        onDismissDialog = {
            viewModel.onDismissDialog()
        }
    )
}

@Composable
fun PayPayReceiptMaskRatioAdjustScreen(
    uiState: PayPayReceiptMaskRatioAdjustUiState,
    snackbarHostState: SnackbarHostState,
    onFABClick: () -> Unit,
    onLeftRatioPercentChange: (Float) -> Unit,
    onTopRatioPercentChange: (Float) -> Unit,
    onSaveClick: () -> Unit,
    onDismissDialog: () -> Unit
) {
    Scaffold(topBar = {
        TopBarView(
            title = "PayPay Receipt Masking Setting",
        )
    }, snackbarHost = {
        SnackbarHost(snackbarHostState)
    }, floatingActionButton = {
        if (uiState.isLoading) {
            /* Nothing is shown */
        } else {
            if (uiState.isValidating) {
                CircularProgressIndicator()
            } else {
                Column {
                    Text(
                        "Validate & Save", style = TextStyle.Default.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    IconButton(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(60.dp),
                        onClick = {
                            onFABClick()
                        },
                        colors = IconButtonDefaults.filledIconButtonColors().copy(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check, contentDescription = "Validate"
                        )
                    }
                }
            }
        }
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = "Please adjust the top and left masking ratio. Hide the top-left logo in the receipt"
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Left masking ratio:%.2f".format(uiState.leftRatio * 100) + "[%]")
                Slider(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .padding(8.dp),
                    value = uiState.leftRatio,
                    onValueChange = {
                        if (uiState.isValidating) {
                            /* Do nothing */
                        } else {
                            onLeftRatioPercentChange(it)
                        }
                    })
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Top masking ratio:%.2f".format(uiState.topRatio * 100) + "[%]")
                Slider(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .padding(8.dp),
                    value = uiState.topRatio,
                    onValueChange = {
                        if (uiState.isValidating) {
                            /* Do nothing */
                        } else {
                            onTopRatioPercentChange(it)
                        }
                    })
            }

            if (uiState.bitmap != null) {
                Image(
                    bitmap = uiState.bitmap.asImageBitmap(),
                    contentDescription = "maskedBitmap",
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text("Unable to load image..")
            }
        }
    }

    if (uiState.showConfirm) {
        ConfirmAlertDialog(
            isLoading = uiState.isLoading,
            onClick = {
                if (uiState.isLoading) {
                    return@ConfirmAlertDialog
                }
                onSaveClick()
            },
            onDismissRequest = {
                if (uiState.isLoading) {
                    return@ConfirmAlertDialog
                }
                onDismissDialog()
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                if (uiState.extractResult == null) {
                    Text("Coding Error: Please contact the developer")
                } else {
                    when (val data = uiState.extractResult.sentData) {
                        is SentData.Expense -> {
                            Text(
                                "Do you save the current masking setting?",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(4.dp)
                            )

                            Text("vvvvvvvvvvvvv Extracted Data vvvvvvvvvvvvv")
                            SentDataExpenseDisplay(data = data)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PayPayReceiptMaskRatioAdjustScreenPreview() {
    val uiState = PayPayReceiptMaskRatioAdjustUiState(
        isLoading = true,
        message = null,
        leftRatio = 0.1f,
        topRatio = 0.2f,
        bitmap = null,

        showConfirm = true,
        extractResult = ExtractedData(
            sentData = SentData.Expense(
                datetime = LocalDateTime.now().toIsoUtcString(ZoneId.systemDefault()),
                amount = null,
                storeName = "McDonald's"
            ),
            bitmap = null
        )
    )
    PayPayReceiptMaskRatioAdjustScreen(
        uiState,
        snackbarHostState = SnackbarHostState(),
        onFABClick = {},
        onLeftRatioPercentChange = {},
        onTopRatioPercentChange = {},
        onSaveClick = {},
        onDismissDialog = {}
    )
}

@Composable
fun SentDataExpenseDisplay(modifier: Modifier = Modifier, data: SentData.Expense) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Text("Date Time: ")
            val datetime = data.datetime
            if (datetime == null) {
                Text(
                    "datetime was not extracted",
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.error
                    )
                )
            } else {
                val dt = datetime.toLocalDateTime(ZoneId.systemDefault())
                Text(
                    "${dt.year}/${dt.monthValue}/${dt.dayOfMonth} ${dt.hour}:${dt.minute}"
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Text("Amount: ")
            val amount = data.amount
            if (amount == null) {
                Text(
                    "amount was not extracted",
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.error
                    )
                )
            } else {
                Text("${amount}")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Text("Store name: ")
            val storeName = data.storeName
            if (storeName == null) {
                Text(
                    "storeName was not extracted",
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.error
                    )
                )
            } else {
                Text(storeName)
            }
        }
    }
}