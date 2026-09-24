package gaku.original.myapplication.ui.screens.bottom.statistics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun StatisticsScreenRoot() {
    StatisticsScreen()
}

@Composable
fun StatisticsScreen() {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text("The App will never be completed. \nIt will continue to grow as long as there is imagination left in the world.")
    }
}

@Preview(showBackground = true)
@Composable
fun StatisticsScreenPreview() {
    StatisticsScreen()
}