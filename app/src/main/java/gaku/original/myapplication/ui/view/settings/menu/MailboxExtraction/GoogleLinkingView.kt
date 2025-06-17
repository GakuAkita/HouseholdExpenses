package gaku.original.myapplication.ui.view.settings.menu.MailboxExtraction

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import gaku.original.myapplication.Screen
import gaku.original.myapplication.data.dataClass.MailboxExtraction
import gaku.original.myapplication.ui.common.TopBarView


@Composable
fun GoogleLinkingView(
    navController: NavController,
) {
    val context = LocalContext.current

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

    fun openOAuthPage(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
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
            Column() {
                Button(onClick = {
                    val oauthUrl =
                        "https://accounts.google.com/o/oauth2/v2/auth?client_id=70966018964-vgpos1q8ufbqbpbvq3aqmjqd67n93q4b.apps.googleusercontent.com&redirect_uri=https://householdexpenses.firebaseapp.com/__/auth/handler&response_type=code&scope=https://www.googleapis.com/auth/gmail.readonly&access_type=offline&prompt=consent"
                    openOAuthPage(
                        context,
                        oauthUrl
                    )
                }) {
                    Text("Gmail API許可")
                }
            }

            MailboxExtractionMenu(
                rakutenPaySetting.menuName,
                onClick = {
                    navController.navigate(Screen.SettingScreen.GoogleLinking.RakutenPay.route)
                }
            )

            MailboxExtractionMenu(
                amazonKindleSetting.menuName,
                onClick = {
                    navController.navigate(Screen.SettingScreen.GoogleLinking.AmazonKindle.route)
                }
            )

            MailboxExtractionMenu(
                amazonItemSetting.menuName,
                onClick = {
                    navController.navigate(Screen.SettingScreen.GoogleLinking.AmazonItem.route)
                }
            )

            MailboxExtractionMenu(
                shikokuElectricSetting.menuName,
                onClick = {
                    navController.navigate(Screen.SettingScreen.GoogleLinking.ShikokuElectricPower.route)
                }
            )
        }
    }
}