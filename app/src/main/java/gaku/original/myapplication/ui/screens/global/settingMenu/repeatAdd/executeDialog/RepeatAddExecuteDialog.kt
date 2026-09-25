package gaku.original.myapplication.ui.screens.global.settingMenu.repeatAdd.executeDialog

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import gaku.original.myapplication.data.dataClass.RepeatFrequency
import gaku.original.myapplication.ui.screens.global.settingMenu.repeatAdd.editDialog.toDisplayName
import java.time.DayOfWeek

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
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.onSecondary
                )
        ) {
            Text(
                "Do you want to add expenses for the rest of this month?",
                fontSize = 20.sp,
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Text("Amount: ${uiState.amount}")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Text("Category: ${uiState.categoryName}")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Text("Frequency: ${uiState.frequency.toDisplayName()}")
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    when (uiState.frequency) {
                        is RepeatFrequency.EveryYear -> {
                            Text("month: ${uiState.frequency.month}")
                            Text("day: ${uiState.frequency.day}")
                        }

                        is RepeatFrequency.EveryMonth -> {
                            Text("day: ${uiState.frequency.day}")
                        }

                        is RepeatFrequency.EveryWeek -> {
                            Text("dayOfWeek: ${uiState.frequency.dayOfWeek}")
                        }

                        else -> {
                            /* do nothing */
                        }
                    }
                }
            }

            if (uiState.isWorking) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text("Progress: ${"%.2f%%".format(uiState.inProgressPercent)}")
                    LinearProgressIndicator(
                        progress = { uiState.inProgressPercent.toFloat() / 100 },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        modifier = Modifier
                            .width(120.dp),
                        onClick = {
                            onNoClick()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text("No")
                    }

                    Button(
                        modifier = Modifier
                            .width(120.dp),
                        onClick = {
                            onYesClick()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                    ) {
                        Text("Yes")
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
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