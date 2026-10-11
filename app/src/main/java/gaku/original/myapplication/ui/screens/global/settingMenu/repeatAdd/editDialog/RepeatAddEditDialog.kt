package gaku.original.myapplication.ui.screens.global.settingMenu.repeatAdd.editDialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.MainGraph
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.RepeatFrequency
import gaku.original.myapplication.ui.common.CategoryDropDown
import gaku.original.myapplication.ui.common.DialogCard
import gaku.original.myapplication.ui.common.IntegerTextField
import gaku.original.myapplication.ui.common.enabledTextFiledColorSet
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun RepeatAddEditDialogRoot(
    navHostController: NavHostController,
    viewModel: RepeatAddEditViewModel = viewModel(factory = RepeatAddEditViewModel.Factory(null))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    /* I still don't understand how snackbar works. What if I define SnackBarHostStae here? */
    //val snackbarHostState = LocalSnackBarHostState.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it, actionLabel = "OK")
        }
        viewModel.onMessageShown()
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            if (uiState.newRepeatAdd == null) {
                navHostController.popBackStack()
            } else {
                /* when newly added, ask the user if they add expense for the rest of the days this month */
                navHostController.navigate(
                    MainGraph.SettingMenu.IRepeatAdd.ExecuteDialog(
                        uiState.newRepeatAdd!!
                    )
                ) {
                    popUpTo<MainGraph.SettingMenu.IRepeatAdd.EditDialog> {
                        inclusive = true
                    }
                }
            }
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
        onMonthChange = {
            viewModel.onMonthChange(it)
        },
        onDayOfWeekChange = { dayOfWeek, status ->
            viewModel.onDayOfWeekChange(dayOfWeek, status)
        },
        onDayChange = {
            viewModel.onDayChange(it)
        },
        onHourChange = {
            viewModel.onHourChange(it)
        },
        onMinuteChange = {
            viewModel.onMinuteChange(it)
        },
        onSaveClick = {
            viewModel.onSaveClick()
        },
        onCancelClick = {
            navHostController.popBackStack()
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
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
    onDayOfWeekChange: (DayOfWeek, Boolean) -> Unit,
    onHourChange: (String) -> Unit,
    onMinuteChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    var frequencyExpanded by remember { mutableStateOf(false) }

    val frequencyValueWidth = 72.dp

    DialogCard(
        title = "Repeat Add",
        snackbarHostState = snackbarHostState,
        actions = {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            } else {
                TextButton(
                    onClick = {
                        onCancelClick()
                    }
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        onSaveClick()
                    }
                ) {
                    Text("Save")
                }
            }
        }
    ) {
        IntegerTextField(
            modifier = Modifier.fillMaxWidth(),
            value = "${uiState.amount ?: ""}",
            onValueChange = { text ->
                /* filter only accept digits */
                if (text.isEmpty() || text.all { it.isDigit() }) {
                    onAmountChange(text)
                }
            },
            label = { Text("Amount(yen)") },
        )

        CategoryDropDown(
            modifier = Modifier.fillMaxWidth(),
            selectedCategory = uiState.category,
            categories = uiState.categories,
            onCategorySelected = {
                onCategorySelected(it)
            }
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.note ?: "",
            onValueChange = {
                onNoteChange(it)
            },
            label = { Text("Note") }
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.itemName ?: "",
            onValueChange = {
                onItemNameChange(it)
            },
            label = { Text("Item name") },
            singleLine = true
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.storeName ?: "",
            onValueChange = {
                onStoreNameChange(it)
            },
            label = { Text("Store name") },
            singleLine = true
        )

        Text(
            text = "Schedule",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        frequencyExpanded = true
                    },
                value = uiState.frequency?.toDisplayName() ?: "",
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text("Frequency") },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
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

        if (uiState.frequency != null) {
            when (uiState.frequency) {
                is RepeatFrequency.EveryYear -> {
                    /* month,day */
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IntegerTextField(
                            modifier = Modifier.width(frequencyValueWidth),
                            value = "${uiState.month ?: ""}",
                            label = { Text("Month") },
                            onValueChange = {
                                onMonthChange(it)
                            }
                        )
                        IntegerTextField(
                            modifier = Modifier.width(frequencyValueWidth),
                            value = "${uiState.day ?: ""}",
                            label = { Text("Day") },
                            onValueChange = {
                                onDayChange(it)
                            }
                        )
                    }
                }

                is RepeatFrequency.EveryMonth -> {
                    /* day,hour,minute */
                    IntegerTextField(
                        modifier = Modifier.width(frequencyValueWidth),
                        value = "${uiState.day ?: ""}",
                        label = { Text("Day") },
                        onValueChange = {
                            onDayChange(it)
                        }
                    )
                }

                is RepeatFrequency.EveryWeek -> {
                    /* List<dayOfWeek> */
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DayOfWeek.entries.forEach {
                            val isSelected = uiState.dayOfWeek.contains(it)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    onDayOfWeekChange(it, !isSelected)
                                },
                                label = {
                                    Text(
                                        it.getDisplayName(
                                            TextStyle.SHORT,
                                            Locale.ENGLISH
                                        )
                                    )
                                }
                            )
                        }
                    }
                }


                is RepeatFrequency.Weekdays,
                is RepeatFrequency.Weekends,
                is RepeatFrequency.Everyday -> {
                    /* do nothing */
                }
            }

            /* hour and minute always here */
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IntegerTextField(
                    modifier = Modifier.width(frequencyValueWidth),
                    value = "${uiState.hour ?: ""}",
                    label = { Text("Hour") },
                    onValueChange = {
                        onHourChange(it)
                    }
                )

                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = ":",
                    style = MaterialTheme.typography.headlineMedium
                )

                IntegerTextField(
                    modifier = Modifier.width(frequencyValueWidth),
                    value = "${uiState.minute ?: ""}",
                    label = { Text("Min") },
                    onValueChange = {
                        onMinuteChange(it)
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun RepeatAddEditDialogPreview() {
    val uiState = RepeatAddEditDialogState(
        frequency = RepeatFrequency.EveryYear(),
        message = "Snackbar message"
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
        onDayOfWeekChange = { _, _ -> },
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


/* e.g. "Every month · day 25 · 09:00" */
fun RepeatFrequency.toDetailText(): String {
    fun time(hour: Int, minute: Int) = "%02d:%02d".format(hour, minute)
    return when (this) {
        is RepeatFrequency.EveryYear -> "${toDisplayName()} · $month/$day · ${time(hour, minute)}"
        is RepeatFrequency.EveryMonth -> "${toDisplayName()} · day $day · ${time(hour, minute)}"
        is RepeatFrequency.EveryWeek -> "${toDisplayName()} · ${
            dayOfWeek.joinToString(", ") { it.getDisplayName(TextStyle.SHORT, Locale.ENGLISH) }
        } · ${time(hour, minute)}"

        is RepeatFrequency.Weekdays -> "${toDisplayName()} · ${time(hour, minute)}"
        is RepeatFrequency.Weekends -> "${toDisplayName()} · ${time(hour, minute)}"
        is RepeatFrequency.Everyday -> "${toDisplayName()} · ${time(hour, minute)}"
    }
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