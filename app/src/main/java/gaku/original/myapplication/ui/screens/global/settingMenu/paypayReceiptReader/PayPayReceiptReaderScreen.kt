package gaku.original.myapplication.ui.screens.global.settingMenu.paypayReceiptReader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.ui.common.ConfirmAlertDialog
import gaku.original.myapplication.ui.common.TopBarView

@Composable
fun PayPayReceiptReaderScreenRoot(
    navHostController: NavHostController,
    viewModel: PayPayReceiptReaderViewModel = viewModel(factory = PayPayReceiptReaderViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackBarHostState.current

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    PayPayReceiptReaderScreen(
        uiState,
        snackbarHostState,
        onBackNavClick = {
            navHostController.popBackStack()
        },
        onResetClick = {
            viewModel.resetSetting()
        }
    )
}

@Composable
fun PayPayReceiptReaderScreen(
    uiState: PayPayReceiptReaderUiState,
    snackbarHostState: SnackbarHostState,
    onBackNavClick: () -> Unit,
    onResetClick: () -> Unit
) {
    var isShowResetDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopBarView(
                title = "PayPay Receipt Reader",
                onBackNavClicked = {
                    onBackNavClick()
                },
                showBackButton = true
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Share a PayPay receipt screenshot to this app to add it as an expense. " +
                        "The PayPay logo in the top-left corner is hidden before the receipt is read.",
                style = MaterialTheme.typography.bodyMedium
            )

            MaskSettingCard(uiState)

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.isMaskSet && !uiState.isLoading,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                onClick = {
                    isShowResetDialog = true
                }
            ) {
                Text("Reset masking setting")
            }

            Text(
                "After a reset, you will be asked to adjust the masking area again " +
                        "the next time you share a PayPay receipt.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (isShowResetDialog) {
        ConfirmAlertDialog(
            onClick = {
                onResetClick()
                isShowResetDialog = false
            },
            onDismissRequest = {
                isShowResetDialog = false
            },
            confirmContent = {
                Text(
                    "Reset the masking setting?",
                    modifier = Modifier.padding(24.dp)
                )
            }
        )
    }
}

@Composable
private fun MaskSettingCard(uiState: PayPayReceiptReaderUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Masking area", style = MaterialTheme.typography.titleMedium)
            HorizontalDivider()
            when {
                uiState.isLoading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.isLoadError -> {
                    Text(
                        "Failed to load the masking setting.",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                uiState.isMaskSet -> {
                    MaskValueRow("Width", uiState.maskWidthPercent!!)
                    MaskValueRow("Height", uiState.maskHeightPercent!!)
                }

                else -> {
                    Text("Not set", color = MaterialTheme.colorScheme.error)
                    Text(
                        "Share a PayPay receipt screenshot to this app. " +
                                "You will be asked to adjust the masking area the first time.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun MaskValueRow(label: String, percent: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Text("%.1f %%".format(percent))
    }
}

@Preview(showBackground = true)
@Composable
fun PayPayReceiptReaderScreenPreview() {
    val uiState = PayPayReceiptReaderUiState(
        isLoading = false,
        message = null,
        maskWidthPercent = 25.5f,
        maskHeightPercent = 8.2f,
        isLoadError = false
    )
    PayPayReceiptReaderScreen(
        uiState,
        SnackbarHostState(),
        onBackNavClick = {},
        onResetClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PayPayReceiptReaderScreenNotSetPreview() {
    PayPayReceiptReaderScreen(
        PayPayReceiptReaderUiState(),
        SnackbarHostState(),
        onBackNavClick = {},
        onResetClick = {}
    )
}
