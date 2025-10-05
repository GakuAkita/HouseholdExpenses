package gaku.original.myapplication.ui.view.settings.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import gaku.original.myapplication.ui.common.TopBarView

@Composable
fun PayPayReceiptOCRSettingView(navController: NavController) {
    Scaffold(
        topBar = {
            TopBarView(
                title = "PayPayレシートOCR設定",
                onBackNavClicked = {
                    navController.popBackStack()
                },
                showBackButton = true
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            
        }

    }
}