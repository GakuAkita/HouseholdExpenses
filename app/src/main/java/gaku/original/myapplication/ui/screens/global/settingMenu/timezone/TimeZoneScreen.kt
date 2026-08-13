package gaku.original.myapplication.ui.screens.global.settingMenu.timezone

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.data.Constants.TimeZone
import gaku.original.myapplication.ui.common.TopBarView

@Composable
fun TimeZoneScreenRoot(
    viewModel: TimeZoneViewModel = viewModel(factory = TimeZoneViewModel.Factory),
    navHostController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = LocalSnackBarHostState.current
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackBarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    TimeZoneScreen(
        uiState,
        snackBarHostState,
        onBackNavClick = {
            navHostController.popBackStack()
        },
        onTimeZoneSelected = {
            viewModel.onTimeZoneSelected(it)
        }
    )
}

@Composable
fun TimeZoneScreen(
    uiState: TimeZoneUiState,
    snackbarHostState: SnackbarHostState,
    onBackNavClick: () -> Unit,
    onTimeZoneSelected: (TimeZone) -> Unit
) {
    /**
     * ZoneId.SystemDefault() should be added so that the user can select.
     */
    Scaffold(
        topBar = {
            TopBarView(
                title = "TimeZone",
                onBackNavClicked = { onBackNavClick() },
                showBackButton = true,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Current TimeZone:")
                Text(uiState.selectedTimeZone.label)
            }

            TimeZoneDropdown(
                modifier = Modifier.widthIn(min = 200.dp),
                selectedOption = uiState.selectedTimeZone,
                onOptionSelected = {
                    onTimeZoneSelected(it)
                }
            )
        }
    }
}

@Preview
@Composable
fun TimeZoneScreenPreview() {

    val uiState = TimeZoneUiState()

    TimeZoneScreen(
        uiState,
        snackbarHostState = SnackbarHostState(),
        onBackNavClick = {},
        onTimeZoneSelected = {}
    )
}

@Composable
fun TimeZoneDropdown(
    modifier: Modifier = Modifier,
    selectedOption: TimeZone,
    onOptionSelected: (TimeZone) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Box(
            modifier = modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable { expanded = true }
                .border(
                    1.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
        ) {
            Text(text = selectedOption.label)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TimeZone.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text("${option.label} ${option.id}") },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
