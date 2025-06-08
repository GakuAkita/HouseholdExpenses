package gaku.original.myapplication.ui.view.settings.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import gaku.original.myapplication.data.dataClass.MailAutoExtraction
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.viewModel.settings.MailAutoExtractionViewModel

@Composable
fun MailAutoExtractionView(
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
                title = "aa"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            Button(
                onClick = {
                    viewModel.setMailAutoExtractionInternalType(test)
                }
            ) {
                Text("aaa")
            }
        }
    }
}