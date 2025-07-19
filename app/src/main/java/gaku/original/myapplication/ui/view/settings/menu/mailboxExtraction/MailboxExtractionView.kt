package gaku.original.myapplication.ui.view.settings.menu.mailboxExtraction

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.Interface.HasCategoryId
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.utility.LogAkitaDebug
import gaku.original.myapplication.viewModel.settings.EmailTemplateSettingState
import gaku.original.myapplication.viewModel.settings.MailboxExtractionViewModel
import kotlinx.coroutines.launch


@Composable
fun MailboxExtractionView(
    navController: NavController,
    viewModel: MailboxExtractionViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val loading by viewModel.loading.collectAsState(false)

    val rakutenPaySettingState by viewModel.rakutenPaySettingState.collectAsState()
    val amazonKindleSettingState by viewModel.amazonKindleSettingState.collectAsState()
    val amazonItemSettingState by viewModel.amazonItemSettingState.collectAsState()
    val shikokuElectricSettingState by viewModel.shikokuElectricPowerSettingState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startInit()
    }

    @Composable
    fun MailboxExtractionMenu(
        settingState: EmailTemplateSettingState,
        onClick: () -> Unit = {}
    ) {
        val settingStatus = settingState.status
        val isSettingStatusSuccess = settingStatus.status == SuspendFuncStatus.SUCCESS
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .border(width = 1.dp, color = MaterialTheme.colorScheme.primary)
                .clickable { onClick() }
        ) {
            Row(
                modifier = Modifier
            ) {
                Text(
                    text = settingState.type.menuName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (isSettingStatusSuccess) {
                        if (settingState.setting?.enabled != null) {
                            Switch(
                                checked = settingState.setting.enabled,
                                onCheckedChange = { checked ->
                                    val updatedState =
                                        settingState.setting.copyWithEnabled(checked)
                                    viewModel.updateEmailTemplateSettingWithLocalUpdate(
                                        settingState.copy(
                                            setting = updatedState,
                                        ),
                                        callback = {}
                                    )
                                }
                            )
                        } else {
                            Text("Something went wrong. Contact the developer")
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    //.padding(horizontal = 5.dp, vertical = 2.dp)
                    .fillMaxWidth()
            ) {
                if (settingState.type is HasCategoryId) {
                    Text(
                        text = "カテゴリを表示するようにする",
                    )
                } else {
                    Text(
                        text = "ここは未設定",
                        modifier = Modifier.padding(5.dp)
                    )
                }
            }
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
            if (loading) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
            } else {
                Column {
                    /**
                     * ここでtokenがすでに存在していれば、
                     * このGmailAPI許可ボタンは消す
                     */
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

                MailboxExtractionMenu(
                    rakutenPaySettingState,
                )

                MailboxExtractionMenu(
                    amazonKindleSettingState,
                )

                MailboxExtractionMenu(
                    amazonItemSettingState,
                )

                MailboxExtractionMenu(
                    shikokuElectricSettingState,
                )
            }
        }
    }
}