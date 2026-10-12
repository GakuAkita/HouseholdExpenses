package gaku.original.myapplication.ui.screens.bottom.statistics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.ui.common.EmptyState


@Composable
fun StatisticsScreenRoot(
    rootNavController: NavHostController,
    viewModel: StatisticsViewModel = viewModel(factory = StatisticsViewModel.Factory)
) {
    StatisticsScreen()
}

@Composable
fun StatisticsScreen() {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        EmptyState(
            icon = Icons.Outlined.Insights,
            title = "Statistics are coming soon",
            message = "The App will never be completed.\nIt will continue to grow as long as there is imagination left in the world."
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StatisticsScreenPreview() {
    StatisticsScreen()
}
