package gaku.original.myapplication.ui.view.settings.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.viewModel.settings.AmazonSubscribeItemsViewModel

@Composable
fun AmazonSubscribeItemsView(
    viewModel: AmazonSubscribeItemsViewModel = hiltViewModel(),
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopBarView(
                "Amazon定期便アイテム",
                showBackButton = true,
                onBackNavClicked = { navController.popBackStack() }
            )
        },
    ) { innerPadding ->
        // ここにコンテンツを追加
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

        }
    }
}
