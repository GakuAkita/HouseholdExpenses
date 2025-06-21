package gaku.original.myapplication.ui.view.settings.menu.GmailLinking

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import gaku.original.myapplication.Screen
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.dataClass.MailboxExtraction
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.utility.LogAkitaDebug
import gaku.original.myapplication.viewModel.settings.GmailLinkingViewModel
import kotlinx.coroutines.launch


@Composable
fun GmailLinkingView(
    navController: NavController,
    viewModel: GmailLinkingViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val rakutenPaySetting = MailboxExtraction.RakutenPay()
    val amazonKindleSetting = MailboxExtraction.AmazonKindle()
    val amazonItemSetting = MailboxExtraction.AmazonItem()
    val shikokuElectricSetting = MailboxExtraction.ShikokuElectricPower()

    val loading by viewModel.loading.collectAsState(false)

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

    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
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
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.Top
        ) {
            Column() {
                /**
                 * ここでtokenがすでに存在していれば、
                 * このGmailAPI許可ボタンは消す
                 */
                if (loading) {
                    CircularProgressIndicator()
                } else {
                    Button(onClick = {
                        viewModel.getOAuthUrl(
                            callback = { status, url ->
                                if (status.status == SuspendFuncStatus.SUCCESS) {
                                    LogAkitaDebug("generated URL:$url")
                                    openOAuthPage(context, url)
                                } else {
                                    snackBarHostState.currentSnackbarData?.dismiss()
                                    scope.launch {
                                        snackBarHostState.showSnackbar(
                                            message = status.errorMessage,
                                            actionLabel = "OK"
                                        )
                                    }
                                }
                            }
                        )
                    }) {
                        Text("Gmail API許可")
                    }
                }
            }

            MailboxExtractionMenu(
                rakutenPaySetting.menuName,
                onClick = {
                    navController.navigate(Screen.SettingScreen.GmailLinking.RakutenPay.route)
                }
            )

            MailboxExtractionMenu(
                amazonKindleSetting.menuName,
                onClick = {
                    navController.navigate(Screen.SettingScreen.GmailLinking.AmazonKindle.route)
                }
            )

            MailboxExtractionMenu(
                amazonItemSetting.menuName,
                onClick = {
                    navController.navigate(Screen.SettingScreen.GmailLinking.AmazonItem.route)
                }
            )

            MailboxExtractionMenu(
                shikokuElectricSetting.menuName,
                onClick = {
                    navController.navigate(Screen.SettingScreen.GmailLinking.ShikokuElectricPower.route)
                }
            )
        }
    }
}