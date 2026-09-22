package gaku.original.myapplication.ui.screens.global.settingMenu.repeatAdd.executeDialog

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController

@Composable
fun RepeatAddExecuteDialogRoot(
    viewModel: RepeatAddExecuteViewModel,
    navHostController: NavHostController
) {
    BackHandler {
        /* Disable horizonal swipe. */
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RepeatAddExecuteDialog(
        uiState,
        onYesClick = {

        },
        onNoClick = {
            navHostController.popBackStack()
        }
    )
}

@Composable
fun RepeatAddExecuteDialog(
    uiState: RepeatAddExecuteUiState,
    onYesClick: () -> Unit,
    onNoClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.onSecondary
            )
    ) {
        Text("Do you want to add the Expense for this month?")

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
                    containerColor = MaterialTheme.colorScheme.primary
                ),
            ) {
                Text("Yes")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RepeatAddExecuteDialogPreview() {
    val uiState = RepeatAddExecuteUiState()
    RepeatAddExecuteDialog(
        uiState,
        onYesClick = {},
        onNoClick = {}
    )
}