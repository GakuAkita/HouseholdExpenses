package gaku.original.myapplication.ui.screens.global.settingMenu.timezone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Dates and times of expenses are shown in this timezone.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TimeZoneOptions(
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
fun TimeZoneOptions(
    modifier: Modifier = Modifier,
    selectedOption: TimeZone,
    onOptionSelected: (TimeZone) -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.selectableGroup()) {
            TimeZone.entries.forEachIndexed { index, option ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .selectable(
                            selected = option == selectedOption,
                            onClick = {
                                if (option != selectedOption) {
                                    onOptionSelected(option)
                                }
                            },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = option == selectedOption,
                        onClick = null,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Column {
                        Text(option.label, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            option.id,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
