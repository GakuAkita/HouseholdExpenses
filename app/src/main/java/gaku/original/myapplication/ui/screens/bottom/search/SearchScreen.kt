package gaku.original.myapplication.ui.screens.bottom.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SearchScreenRoot(
    viewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SearchScreen(
        uiState
    )
}

@Composable
fun SearchScreen(
    uiState: SearchUiState
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Text("Search", modifier = Modifier.padding(8.dp))
            }
        }
        Text("The App will never be completed. \nIt will continue to grow as long as there is imagination left in the world.")
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    val uiState = SearchUiState()
    SearchScreen(
        uiState
    )
}