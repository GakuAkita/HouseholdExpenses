package gaku.original.myapplication.ui.screens.bottom.setting.menu.userInfo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.ui.common.TopBarView

@Composable
fun UserInfoScreenRoot(
    navHostController: NavHostController,
    viewModel: UserInfoViewModel = viewModel(factory = UserInfoViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
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
                .padding(
                    top = 30.dp,
                )
                .padding(horizontal = 10.dp)
        ) {
            Row {
                Text("Email:")
                Text(uiState.email)
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