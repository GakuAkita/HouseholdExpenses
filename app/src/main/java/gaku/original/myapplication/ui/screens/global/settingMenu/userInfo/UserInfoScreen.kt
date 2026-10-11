package gaku.original.myapplication.ui.screens.global.settingMenu.userInfo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.ui.common.InfoRow
import gaku.original.myapplication.ui.common.TopBarView

@Composable
fun UserInfoScreenRoot(
    navHostController: NavHostController,
    viewModel: UserInfoViewModel = viewModel(factory = UserInfoViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackBarHostState.current

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    UserInfoScreen(
        uiState,
        snackbarHostState = snackbarHostState,
        onBackNavClick = {
            navHostController.popBackStack()
        }
    )
}

@Composable
fun UserInfoScreen(
    uiState: UserInfoUiState,
    snackbarHostState: SnackbarHostState,
    onBackNavClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopBarView(
                title = "User information",
                onBackNavClicked = { onBackNavClick() },
                showBackButton = true,
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
                .padding(16.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                InfoRow(
                    label = "Email",
                    value = uiState.email,
                    icon = Icons.Outlined.Email
                )
            }
        }
    }
}

@Preview
@Composable
fun UserInfoScreenPreview() {

    val uiState = UserInfoUiState()

    UserInfoScreen(
        uiState,
        snackbarHostState = SnackbarHostState(),
        onBackNavClick = {}
    )
}