package gaku.original.myapplication.ui.view.settings.menu.MailboxExtraction

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
import gaku.original.myapplication.data.dataClass.MailboxExtraction
import gaku.original.myapplication.ui.common.TopBarView


@Composable
fun MailboxExtractionView(
    navController: NavController,
) {
    val rakutenPaySetting = MailboxExtraction.RakutenPay()
    val amazonKindleSetting = MailboxExtraction.AmazonKindle()
    val amazonItemSetting = MailboxExtraction.AmazonItem()
    val shikokuElectricSetting = MailboxExtraction.ShikokuElectricPower()

    @Composable
    fun MailboxExtractionMenu(
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
            MailboxExtractionMenu(
                rakutenPaySetting.menuName,
                onClick = {
                    navController.navigate(Screen.SettingScreen.MailboxExtraction.RakutenPay.route)
                }
            )

            MailboxExtractionMenu(
                amazonKindleSetting.menuName,
                onClick = {
                    navController.navigate(Screen.SettingScreen.MailboxExtraction.AmazonKindle.route)
                }
            )

            MailboxExtractionMenu(
                amazonItemSetting.menuName,
                onClick = {
                    navController.navigate(Screen.SettingScreen.MailboxExtraction.AmazonItem.route)
                }
            )

            MailboxExtractionMenu(
                shikokuElectricSetting.menuName,
                onClick = {
                    navController.navigate(Screen.SettingScreen.MailboxExtraction.ShikokuElectricPower.route)
                }
            )
        }
    }
}