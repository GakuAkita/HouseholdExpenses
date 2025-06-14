package gaku.original.myapplication.ui.view.settings.menu.MailAutoExtraction

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import gaku.original.myapplication.Screen
import gaku.original.myapplication.data.dataClass.MailAutoExtraction
import gaku.original.myapplication.ui.common.TopBarView


@Composable
fun MailAutoExtractionView(
    navController: NavController,
) {
    val rakutenPaySetting = MailAutoExtraction.RakutenPay()
    val amazonKindleSetting = MailAutoExtraction.AmazonKindle()
    val amazonItemSetting = MailAutoExtraction.AmazonItem()
    val shikokuElectricSetting = MailAutoExtraction.ShikokuElectricPower()

    @Composable
    fun MailAutoExtractionMenu(
        menuName: String,
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
            Text(menuName, modifier = Modifier.padding(textPad))
        }
    }

    /******* UI ******/
    Scaffold(
        topBar = {
            TopBarView(
                title = "メールボックス自動連携",
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
                rakutenPaySetting.menuName,
                onClick = {
                    navController.navigate(Screen.SettingScreen.MailAutoExtraction.RakutenPay.route)
                }
            )

            MailAutoExtractionMenu(
                amazonKindleSetting.menuName,
                onClick = {
                    navController.navigate(Screen.SettingScreen.MailAutoExtraction.AmazonKindle.route)
                }
            )

            MailAutoExtractionMenu(
                amazonItemSetting.menuName,
                onClick = {
                    navController.navigate(Screen.SettingScreen.MailAutoExtraction.AmazonItem.route)
                }
            )

            MailAutoExtractionMenu(
                shikokuElectricSetting.menuName,
                onClick = {
                    navController.navigate(Screen.SettingScreen.MailAutoExtraction.ShikokuElectricPower.route)
                }
            )
        }
    }
}