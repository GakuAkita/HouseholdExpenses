package gaku.original.myapplication.ui.screens.bottom.setting.menu.timezone

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.data.AppTimeZone
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.Constants.TimeZoneOption
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.viewModel.settings.AppSettingsViewModel
import kotlinx.coroutines.launch

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
            //viewModel.onMessageShown()
        }
    }

    TimeZoneScreen(
        uiState,
        snackBarHostState,
        onBackNavClick = {
            navHostController.popBackStack()
        }
    )
}

@Composable
fun TimeZoneScreen(
    uiState: TimeZoneUiState,
    snackbarHostState: SnackbarHostState,
    onBackNavClick: () -> Unit
) {

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
        ) {

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
        onBackNavClick = {}
    )
}


@Composable
fun AppSettingsView(
    viewModel: AppSettingsViewModel = hiltViewModel(),
    navController: NavController
) {
    var expanded by remember { mutableStateOf(false) }

    val zoneId by AppTimeZone.zoneIdFlow.collectAsState()

    // 現在のZoneIdに一致するTimeZoneOptionを取得（なければJAPANをデフォルト）a
    var selectedOption by remember(zoneId) {
        mutableStateOf(TimeZoneOption.entries.find { it.id == zoneId.id } ?: TimeZoneOption.JAPAN)
    }

    val scope = rememberCoroutineScope()
    val snackBarHostState = remember {
        SnackbarHostState()
    }

    Scaffold(
        topBar = {
            TopBarView(
                "タイムゾーン設定",
                onBackNavClicked = { navController.popBackStack() },
                showBackButton = true,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            Text("現在の設定:${zoneId}")

            TimeZoneDropdown(
                selectedOption = selectedOption,
                onOptionSelected = { option ->
                    selectedOption = option
                    AppTimeZone.updateStrZoneId(option.id)  // 選択されたタイムゾーンIDを更新
                    viewModel.setUserTimeZone(option.id) { statusInfo ->
                        if (statusInfo.status != FuncStatus.SUCCESS) {
                            scope.launch {
                                snackBarHostState.showSnackbar(
                                    "タイムゾーンの設定に失敗しました: ${statusInfo.errorMessage}\n再度選択してください"
                                )
                            }
                        }
                    }
                }
            )

        }
    }
}

@Composable
fun TimeZoneDropdown(
    selectedOption: TimeZoneOption,
    onOptionSelected: (TimeZoneOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable { expanded = true }
                .border(
                    1.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
        ) {
            Text(text = "${selectedOption.label} ${selectedOption.id}")
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TimeZoneOption.entries.forEach { option ->
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
