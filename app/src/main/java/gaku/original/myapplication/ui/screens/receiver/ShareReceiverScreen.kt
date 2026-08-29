package gaku.original.myapplication.ui.screens.receiver

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import gaku.original.myapplication.SharedData
import gaku.original.myapplication.ui.common.TopBarView

@Composable
fun ShareReceiverScreenRoot(
    viewModel: ShareReceiverViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    ShareReceiverScreen(
        uiState
    )
}

@Composable
fun ShareReceiverScreen(
    uiState: ShareReceiverUiState
) {

    Scaffold(
        topBar = {
            TopBarView(
                title = "Received Data",
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text("Received data is here")

            Text("${uiState.sharedData?.packageName}")

            uiState.maskedBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Masked image"
                )
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun ShareReceiverScreenPreview() {
    val uiState = ShareReceiverUiState(
        sharedData = SharedData.Image(
            "jp.ne.paypay.android",
            "https://example.com/image.jpg"
        ),
        maskedBitmap = null
    )
    ShareReceiverScreen(
        uiState
    )
}