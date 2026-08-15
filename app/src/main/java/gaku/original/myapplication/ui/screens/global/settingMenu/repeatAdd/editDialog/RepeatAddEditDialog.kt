package gaku.original.myapplication.ui.screens.global.settingMenu.repeatAdd.editDialog

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.RepeatFrequency
import gaku.original.myapplication.ui.common.CancelButton
import gaku.original.myapplication.ui.common.CategoryDropDown
import gaku.original.myapplication.ui.common.enabledTextFiledColorSet

@Composable
fun RepeatAddEditDialogRoot(
    navHostController: NavHostController,
    viewModel: RepeatAddEditViewModel = viewModel(factory = RepeatAddEditViewModel.Factory(null))
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    /* I still don't understand how snackbar works. What if I define SnackBarHostStae here? */
    val snackbarHostState = LocalSnackBarHostState.current

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            /* Because this is dialog.. */
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    RepeatAddEditDialog(
        uiState,
        snackbarHostState = snackbarHostState,
        onAmountChange = {
            viewModel.onAmountChange(it)
        },
        onCategorySelected = {
            viewModel.onCategorySelected(it)
        },
        onNoteChange = {
            viewModel.onNoteChange(it)
        },
        onItemNameChange = {
            viewModel.onItemNameChange(it)
        },
        onStoreNameChange = {
            viewModel.onStoreNameChange(it)
        },
        onRepeatFrequencySelected = {
            viewModel.onRepeatFrequencySelected(it)
        },
        onMonthChange = {},
        onDayChange = {},
        onHourChange = {
            viewModel.onHourChange(it)
        },
        onMinuteChange = {
            viewModel.onMinuteChange(it)
        },
        onSaveClick = {

        },
        onCancelClick = {
            navHostController.popBackStack()
        }
    )
}

@Composable
fun RepeatAddEditDialog(
    uiState: RepeatAddEditDialogState,
    snackbarHostState: SnackbarHostState,
    onAmountChange: (String) -> Unit,
    onCategorySelected: (Category?) -> Unit,
    onNoteChange: (String?) -> Unit,
    onItemNameChange: (String?) -> Unit,
    onStoreNameChange: (String?) -> Unit,
    onRepeatFrequencySelected: (RepeatFrequency) -> Unit,
    onMonthChange: (String) -> Unit,
    onDayChange: (String) -> Unit,
    onHourChange: (String) -> Unit,
    onMinuteChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    var frequencyExpanded by remember { mutableStateOf(false) }

    val frequencyValueWidth = 40.dp

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(
                        alpha = 0.6f
                    )
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                .padding(4.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Center
        ) {
            TextField(
                value = "${uiState.amount ?: ""}",
                onValueChange = { text ->
                    /* filter only accept digits */
                    if (text.isEmpty() || text.all { it.isDigit() }) {
                        onAmountChange(text)
                    }
                },
                label = { Text("Amount(yen)") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )

            CategoryDropDown(
                initialCategory = uiState.category,
                categories = uiState.categories,
                onCategorySelected = {
                    onCategorySelected(it)
                },

                )

            TextField(
                value = uiState.note ?: "",
                onValueChange = {
                    onNoteChange(it)
                },
                label = { Text("Note") }
            )

            TextField(
                value = uiState.itemName ?: "",
                onValueChange = {
                    onItemNameChange(it)
                },
                label = { Text("Item name") }
            )

            TextField(
                value = uiState.storeName ?: "",
                onValueChange = {
                    onStoreNameChange(it)
                },
                label = { Text("Store name") }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clickable {
                        frequencyExpanded = true
                    }) {
                TextField(
                    value = uiState.frequency?.toDisplayName() ?: "",
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text("Frequency") },
                    colors = enabledTextFiledColorSet()
                )
                DropdownMenu(
                    expanded = frequencyExpanded,
                    onDismissRequest = {
                        frequencyExpanded = false
                    }
                ) {
                    RepeatFrequency.types.forEach { freq ->
                        RepeatFrequencyDropDownMenuItem(
                            freq
                        ) {
                            onRepeatFrequencySelected(it)
                            frequencyExpanded = false
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (uiState.frequency != null) {
                    when (uiState.frequency) {
                        is RepeatFrequency.EveryYear -> {
                            /* month,day */
                            Row {
                                Column(
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("month")
                                    TextField(
                                        modifier = Modifier.width(frequencyValueWidth),
                                        value = "${uiState.month ?: ""}",
                                        onValueChange = {
                                            onMonthChange(it)
                                        }
                                    )
                                }

                                Column(
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("day")
                                    TextField(
                                        modifier = Modifier.width(frequencyValueWidth),
                                        value = "${uiState.day ?: ""}",
                                        onValueChange = {
                                            onDayChange(it)
                                        }
                                    )
                                }
                            }
                        }

                        is RepeatFrequency.EveryMonth -> {
                            /* day,hour,minute */
                            Row {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("day")
                                    TextField(
                                        modifier = Modifier.width(frequencyValueWidth),
                                        value = "${uiState.day ?: ""}",
                                        onValueChange = {
                                            onDayChange(it)
                                        }
                                    )
                                }
                            }
                        }

                        is RepeatFrequency.EveryWeek -> {
                            /* List<dayOfWeek> */
                        }


                        is RepeatFrequency.Weekdays,
                        is RepeatFrequency.Weekends,
                        is RepeatFrequency.Everyday -> {
                            /* do nothing */
                        }
                    }
                }

                if (uiState.frequency != null) {
                    Row {
                        /* hour and minute always here */
                        Column {
                            Text("Hour")
                            TextField(
                                modifier = Modifier.width(frequencyValueWidth),
                                value = "${uiState.hour ?: ""}",
                                onValueChange = {
                                    onHourChange(it)
                                }
                            )
                        }

                        Text(
                            modifier = Modifier.padding(top = 16.dp, start = 4.dp, end = 4.dp),
                            text = ":",
                            fontSize = 40.sp
                        )
                        Column {
                            Text("minute")
                            TextField(
                                modifier = Modifier.width(frequencyValueWidth),
                                value = "${uiState.minute ?: ""}",
                                onValueChange = {
                                    onMinuteChange(it)
                                }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CancelButton(
                    onClick = {
                        onCancelClick()
                    }
                )

                Button(
                    onClick = {
                        onSaveClick()
                    }
                ) {
                    Text("Save")
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState
        )
    }
}

@Preview
@Composable
fun RepeatAddEditDialogPreview() {
    val uiState = RepeatAddEditDialogState(
        frequency = RepeatFrequency.EveryMonth()
    )

    RepeatAddEditDialog(
        uiState,
        snackbarHostState = SnackbarHostState(),
        onAmountChange = {},
        onCategorySelected = {},
        onNoteChange = {},
        onItemNameChange = {},
        onStoreNameChange = {},
        onRepeatFrequencySelected = {},
        onMonthChange = {},
        onDayChange = {},
        onHourChange = {},
        onMinuteChange = {},
        onSaveClick = {},
        onCancelClick = {}
    )
}

fun RepeatFrequency.toDisplayName(): String =
    when (this) {
        is RepeatFrequency.EveryYear -> "Every year"
        is RepeatFrequency.EveryMonth -> "Every month"
        is RepeatFrequency.EveryWeek -> "Every week"
        is RepeatFrequency.Weekdays -> "Weekdays"
        is RepeatFrequency.Weekends -> "Weekends"
        is RepeatFrequency.Everyday -> "Everyday"
    }

@Composable
fun RepeatFrequencyDropDownMenuItem(freq: RepeatFrequency, onClick: (RepeatFrequency) -> Unit) {
    DropdownMenuItem(
        text = {
            Text(freq.toDisplayName())
        },
        onClick = {
            onClick(freq)
        }
    )
}