package gaku.original.myapplication.ui.screens.receiver.paypayReceiptMask

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackBarHostState.current
    val context = LocalContext.current

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navHostController.popBackStack()
            Toast.makeText(context, "Setting was saved. Please analyze again", Toast.LENGTH_SHORT)
                .show()
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
        onSaveClick = {
            viewModel.onSaveClick()
        },
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
            title = "PayPay Receipt Masking",
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
                ExtendedFloatingActionButton(
                    onClick = {
                        onFABClick()
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Check, contentDescription = "Validate"
                        )
                    },
                    text = { Text("Validate & Save") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "Please adjust the top and left masking ratio. Hide the top-left logo in the receipt",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            MaskRatioSlider(
                label = "Left masking ratio",
                value = uiState.leftRatio,
                onValueChange = {
                    if (uiState.isValidating) {
                        /* Do nothing */
                    } else {
                        onLeftRatioPercentChange(it)
                    }
                }
            )

            MaskRatioSlider(
                label = "Top masking ratio",
                value = uiState.topRatio,
                onValueChange = {
                    if (uiState.isValidating) {
                        /* Do nothing */
                    } else {
                        onTopRatioPercentChange(it)
                    }
                }
            )

            if (uiState.bitmap != null) {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 80.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Image(
                        bitmap = uiState.bitmap.asImageBitmap(),
                        contentDescription = "maskedBitmap",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Text(
                    "Unable to load image..",
                    color = MaterialTheme.colorScheme.error
                )
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
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.extractResult == null) {
                    Text("Coding Error: Please contact the developer")
                } else {
                    when (val data = uiState.extractResult.sentData) {
                        is SentData.Expense -> {
                            Text(
                                "Do you save the current masking setting?",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Extracted data",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            SentDataExpenseDisplay(data = data)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MaskRatioSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(
                "%.2f %%".format(value * 100),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange
        )
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
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val datetime = data.datetime
            ExtractedValueRow(
                label = "Date Time",
                value = datetime?.let {
                    val dt = it.toLocalDateTime(ZoneId.systemDefault())
                    "%d/%02d/%02d %02d:%02d".format(dt.year, dt.monthValue, dt.dayOfMonth, dt.hour, dt.minute)
                },
                missingText = "datetime was not extracted"
            )

            ExtractedValueRow(
                label = "Amount",
                value = data.amount?.let { "¥%,d".format(it) },
                missingText = "amount was not extracted"
            )

            ExtractedValueRow(
                label = "Store name",
                value = data.storeName,
                missingText = "storeName was not extracted"
            )
        }
    }
}

@Composable
private fun ExtractedValueRow(
    label: String,
    value: String?,
    missingText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        if (value == null) {
            Text(
                missingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
