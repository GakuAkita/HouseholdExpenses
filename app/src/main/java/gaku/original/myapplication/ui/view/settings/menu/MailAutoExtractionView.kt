package gaku.original.myapplication.ui.view.settings.menu

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import gaku.original.myapplication.data.dataClass.MailAutoExtraction
import gaku.original.myapplication.data.dataClass.MailAutoExtractionCommon
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.viewModel.settings.MailAutoExtractionViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
    var selectedItem by remember { mutableStateOf<String?>(null) }

    var rakutenPaySetting by remember {
        mutableStateOf(
            //初期値
            MailAutoExtraction.RakutenPay(enabled = false)
        )
    }
    var amazonKindleSetting by remember {
        mutableStateOf(
            MailAutoExtraction.AmazonKindle(
                enabled = false
            )
        )
    }
    var amazonItemSetting by remember {
        mutableStateOf(
            MailAutoExtraction.AmazonItem(
                enabled = false
            )
        )
    }
    var shikokuElectricSetting by remember {
        mutableStateOf(
            MailAutoExtraction.ShikokuElectricPower(
                enabled = false
            )
        )
    }

    var loading by remember { mutableStateOf(false) }

    @Composable
    fun MailAutoExtractionMenu(
        setting: MailAutoExtractionCommon,
        onClick: () -> Unit = {}
    ) {
        Row(
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
                .clickable { onClick() }
                .border(width = 1.dp, color = MaterialTheme.colorScheme.primary)
        ) {
            val textPad = 20.dp
            if (setting.enabled) {
                Text(setting.menuName, modifier = Modifier.padding(textPad))
            } else {
                Text("${setting.menuName} (未設定)", modifier = Modifier.padding(textPad))
            }
        }
    }

    @Composable
    fun SettingEditDialog(
        setting: String?,
        onDismiss: () -> Unit = {
            selectedItem = null
        },
    ) {
        BasicAlertDialog(
            onDismissRequest = { onDismiss() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 50.dp, bottom = 50.dp)
            ) {
                val arr = Array<Int>(20, { i -> i })
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    arr.forEach {
                        Text("test$it", modifier = Modifier.height(50.dp))
                    }
                }
            }

            /* 各Dialogの中身は外で定義 */
            when (setting) {
                rakutenPaySetting.menuName -> {

                }

                amazonKindleSetting.menuName -> {

                }

                amazonItemSetting.menuName -> {

                }

                shikokuElectricSetting.menuName -> {

                }

                else -> {

                }
            }
        }

    }


    /******* UI ******/
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
            MailAutoExtractionMenu(
                rakutenPaySetting,
                onClick = {
                    selectedItem = rakutenPaySetting.menuName
                }
            )

            MailAutoExtractionMenu(
                amazonKindleSetting,
                onClick = {
                    selectedItem = amazonKindleSetting.menuName
                }
            )

            MailAutoExtractionMenu(
                amazonItemSetting,
                onClick = {
                    selectedItem = amazonItemSetting.menuName
                }
            )

            MailAutoExtractionMenu(
                shikokuElectricSetting,
                onClick = {
                    selectedItem = shikokuElectricSetting.menuName
                }
            )


            Button(
                onClick = {
                    viewModel.setMailAutoExtractionInternalSetting(test, callback = {})
                }
            ) {
                Text("aaa")
            }

            if (selectedItem != null) {
                SettingEditDialog(selectedItem)
            }
        }
    }
}

@Composable
fun RakutenPaySetting() {

}