package gaku.original.myapplication.ui.view.settings.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import gaku.original.myapplication.data.dataClass.MailAutoExtraction
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.viewModel.settings.MailAutoExtractionViewModel

@Composable
fun MailAutoExtractionView(
    navController: NavController,
    viewModel: MailAutoExtractionViewModel = hiltViewModel()
) {

    val test: MailAutoExtraction.RakutenPay = MailAutoExtraction.RakutenPay(
        enabled = true,
        shopCategoryAssignments = mapOf(
            "shop1" to "category1",
            "shop2" to "category2"
        )
    )
    Scaffold(
        topBar = {
            TopBarView(
                title = "aa",
                showBackButton = true,
                onBackNavClicked = {
                    navController.popBackStack()
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.Top
        ) {
            Text("楽天Pay")
            Text("Amazon Kindle")
            Text("Amazon　品物")
            Text("四国電力")
            Button(
                onClick = {
                    viewModel.setMailAutoExtractionInternalSetting(test, callback = {})
                }
            ) {
                Text("aaa")
            }
        }
    }
}