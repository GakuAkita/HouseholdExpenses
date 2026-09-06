package gaku.original.myapplication.ui.screens.receiver.paypayReceiptMask

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
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
import androidx.navigation.NavHostController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.ui.common.TopBarView

@Composable
fun PayPayReceiptMaskRatioAdjustScreenRoot(
    navHostController: NavHostController,
    viewModel: PayPayReceiptMaskRatioAdjustViewModel
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
        onFABClick = {},
        onLeftRatioPercentChange = {
            viewModel.onLeftRatioChane(it)
        },
        onTopRatioPercentChange = {
            viewModel.onTopRatioChange(it)
        }
    )
}

@Composable
fun PayPayReceiptMaskRatioAdjustScreen(
    uiState: PayPayReceiptMaskRatioAdjustUiState,
    snackbarHostState: SnackbarHostState,
    onFABClick: () -> Unit,
    onLeftRatioPercentChange: (Float) -> Unit,
    onTopRatioPercentChange: (Float) -> Unit
) {
    Scaffold(
        topBar = {
            TopBarView(
                title = "PayPay Receipt Masking Setting",
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        floatingActionButton = {
            Column {
                Text(
                    "Validate", style = TextStyle.Default.copy(
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                IconButton(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(60.dp),
                    onClick = {
                    },
                    colors = IconButtonDefaults.filledIconButtonColors().copy(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Validate"
                    )
                }
            }
        }
    ) { innerPadding ->
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
                        onLeftRatioPercentChange(it)
                    }
                )
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
                        onTopRatioPercentChange(it)
                    }
                )
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
}

@Preview(showBackground = true)
@Composable
fun PayPayReceiptMaskRatioAdjustScreenPreview() {
    val uiState = PayPayReceiptMaskRatioAdjustUiState(
        isLoading = false,
        message = null,
        leftRatio = 0.1f,
        topRatio = 0.2f,
        bitmap = null
    )
    PayPayReceiptMaskRatioAdjustScreen(
        uiState,
        snackbarHostState = SnackbarHostState(),
        onFABClick = {},
        onLeftRatioPercentChange = {},
        onTopRatioPercentChange = {}
    )
}