package gaku.original.myapplication.ui.screens.global.settingMenu.repeatAdd.executeDialog

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import gaku.original.myapplication.data.dataClass.RepeatFrequency
import gaku.original.myapplication.ui.common.DialogCard
import gaku.original.myapplication.ui.common.toYen
import gaku.original.myapplication.ui.screens.global.settingMenu.repeatAdd.editDialog.toDisplayName
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun RepeatAddExecuteDialogRoot(
    viewModel: RepeatAddExecuteViewModel,
    navHostController: NavHostController
) {
    BackHandler {
        /* Disable horizonal swipe. */
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = SnackbarHostState()

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it, actionLabel = "OK")
            viewModel.onMessageShown()
        }
    }

    LaunchedEffect(uiState.isDone) {
        if (uiState.isDone) {
            navHostController.popBackStack()
        }
    }

    RepeatAddExecuteDialog(
        uiState,
        snackbarHostState = snackbarHostState,
        onYesClick = {
            viewModel.onYesClick()
        },
        onNoClick = {
            navHostController.popBackStack()
        }
    )
}

@Composable
fun RepeatAddExecuteDialog(
    uiState: RepeatAddExecuteUiState,
    snackbarHostState: SnackbarHostState,
    onYesClick: () -> Unit,
    onNoClick: () -> Unit
) {
    DialogCard(
        title = "Add for this month?",
        snackbarHostState = snackbarHostState,
        actions = {
            if (!uiState.isWorking) {
                TextButton(
                    onClick = {
                        onNoClick()
                    }
                ) {
                    Text("No")
                }

                Button(
                    onClick = {
                        onYesClick()
                    }
                ) {
                    Text("Yes")
                }
            }
        }
    ) {
        Text(
            "Do you want to add expenses for the rest of this month?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailRow(label = "Amount", value = uiState.amount.toYen())
                DetailRow(label = "Category", value = uiState.categoryName)
                DetailRow(label = "Frequency", value = uiState.frequency.toDisplayName())
                when (uiState.frequency) {
                    is RepeatFrequency.EveryYear -> {
                        DetailRow(label = "Month", value = "${uiState.frequency.month}")
                        DetailRow(label = "Day", value = "${uiState.frequency.day}")
                    }

                    is RepeatFrequency.EveryMonth -> {
                        DetailRow(label = "Day", value = "${uiState.frequency.day}")
                    }

                    is RepeatFrequency.EveryWeek -> {
                        DetailRow(
                            label = "Day of week",
                            value = uiState.frequency.dayOfWeek.joinToString(", ") {
                                it.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                            }
                        )
                    }

                    else -> {
                        /* do nothing */
                    }
                }
            }
        }

        if (uiState.isWorking) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Progress: ${"%.2f%%".format(uiState.inProgressPercent)}",
                    style = MaterialTheme.typography.labelLarge
                )
                LinearProgressIndicator(
                    progress = { uiState.inProgressPercent.toFloat() / 100 },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RepeatAddExecuteDialogPreview() {
    val uiState = RepeatAddExecuteUiState(
        inProgressPercent = 100.0,
        isWorking = true,
        amount = 1000,
        categoryName = "This is Category",
        frequency = RepeatFrequency.EveryWeek(
            dayOfWeek = listOf(
                DayOfWeek.MONDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY,
                DayOfWeek.FRIDAY
            ),
            hour = 0,
            minute = 0
        )
    )
    RepeatAddExecuteDialog(
        uiState,
        snackbarHostState = SnackbarHostState(),
        onYesClick = {},
        onNoClick = {}
    )
}