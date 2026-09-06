package gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction

import android.content.Intent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.MainGraph
import gaku.original.myapplication.data.Interface.HasCategoryId
import gaku.original.myapplication.ui.common.CategoryDropDown
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType.AmazonItem
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType.AmazonKindle
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType.AmazonSubscribe
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType.RakutenCardETC
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType.RakutenPay
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType.ShikokuElectricPower
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType.Udemy
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

val EmailTemplateType.displayName: String
    get() = when (this) {
        is RakutenPay -> "Rakuten Pay"
        is AmazonKindle -> "Amazon Kindle"
        is AmazonItem -> "Amazon Item"
        is AmazonSubscribe -> "Amazon Subscribe"
        is ShikokuElectricPower -> "Shikoku Electric Power"
        is Udemy -> "Udemy"
        is RakutenCardETC -> "Rakuten Card ETC"
    }

@Composable
fun MailboxExtractionScreenRoot(
    navHostController: NavHostController,
    viewModel: MailboxExtractionViewModel = viewModel(factory = MailboxExtractionViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = LocalSnackBarHostState.current

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is MailboxExtractionUiEffect.OpenUrl -> {
                    val url = event.url
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    context.startActivity(intent)
                }
            }
        }
    }

    // https://developer.android.com/develop/ui/compose/side-effects?hl=ja#disposableeffect
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onResume()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Timber.d("message:=${it}")
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }
    MailboxExtractionScreen(
        uiState,
        snackbarHostState,
        onBackNavClick = {
            navHostController.popBackStack()
        },
        onGmailConnectClick = {
            viewModel.onGmailConnectClick()
        },
        onSwitchClick = {
            viewModel.onSwitchClick(it)
        },
        onCategoryAssignmentClick = {
            navHostController.navigate(MainGraph.Global.CategoryAssignment)
        },
        onCategorySelect = { state, categoryId ->
            viewModel.onCategorySelect(state, categoryId)
        }
    )
}

@Composable
fun MailboxExtractionScreen(
    uiState: MailboxExtractionUiState,
    snackbarHostState: SnackbarHostState,
    onBackNavClick: () -> Unit,
    onGmailConnectClick: () -> Unit,
    onSwitchClick: (EmailTemplateUiState<EmailTemplateType>) -> Unit,
    onCategoryAssignmentClick: () -> Unit,
    onCategorySelect: (EmailTemplateUiState<EmailTemplateType>, String?) -> Unit
) {
    Scaffold(
        topBar = {
            TopBarView(
                title = "Mailbox Connection",
                showBackButton = true,
                onBackNavClicked = {
                    onBackNavClick()
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            if (uiState.isLoading || uiState.isWaitingForAuth) {
                CircularProgressIndicator()
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            onGmailConnectClick()
                        }
                    ) {
                        if (uiState.isGmailConnected) {
                            Text("Connect Gmail. (Already done)")
                        } else {
                            Text("Connect Gmail")
                        }
                    }
                }

                if (uiState.isGmailConnected) {
                    uiState.emailTemplateTypeList.forEach { typeUiState ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                                .border(
                                    1.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                ) {
                                    Text(typeUiState.type.displayName)
                                }

                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        modifier = Modifier.padding(4.dp),
                                        text = "Enable:"
                                    )
                                    Switch(
                                        checked = typeUiState.type.enabled,
                                        onCheckedChange = {
                                            onSwitchClick(typeUiState)
                                        },
                                        enabled = !typeUiState.isLoading
                                    )
                                    if (typeUiState.isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            if (typeUiState.type is HasCategoryId<*>) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    CategoryDropDown(
                                        selectedCategoryId = typeUiState.type.categoryId,
                                        nullOption = true,
                                        categories = uiState.categories,
                                        onCategorySelected = { category ->
                                            val categoryId = category.id
                                            onCategorySelect(
                                                typeUiState,
                                                categoryId
                                            )
                                        },
                                        enabled = !typeUiState.isLoading,
                                        modifier = Modifier
                                            .width(280.dp)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            onCategoryAssignmentClick()
                                        }
                                    ) {
                                        Text("Category Assignment")
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Default.OpenInNew,
                                            contentDescription = "Go to Category Assignment"
                                        )
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MailboxExtractionScreenPreview() {
    val uiState = MailboxExtractionUiState(
        isGmailConnected = true,
        emailTemplateTypeList = listOf(
            EmailTemplateUiState(
                type = RakutenPay(
                    enabled = true
                ),
                isLoading = true
            ),
            EmailTemplateUiState(
                type = AmazonKindle(
                    enabled = false
                )
            ),
            EmailTemplateUiState(
                type = AmazonItem(
                    enabled = false
                )
            ),
            EmailTemplateUiState(
                type = AmazonSubscribe(
                    enabled = false
                )
            ),
            EmailTemplateUiState(
                type = ShikokuElectricPower(
                    enabled = false
                )
            ),
        )
    )
    MailboxExtractionScreen(
        uiState,
        snackbarHostState = SnackbarHostState(),
        onBackNavClick = {},
        onGmailConnectClick = {},
        onSwitchClick = {},
        onCategoryAssignmentClick = {},
        onCategorySelect = { _, _ -> }
    )
}
