package gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.MainGraph
import gaku.original.myapplication.data.Interface.HasCategoryId
import gaku.original.myapplication.ui.common.CategoryDropDown
import gaku.original.myapplication.ui.common.LoadingBox
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType.AmazonItem
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType.AmazonKindle
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType.AmazonSubscribe
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType.RakutenCardAll
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType.RakutenCardETC
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType.RakutenPay
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType.ShikokuElectricPower
import gaku.original.myapplication.ui.screens.global.settingMenu.mailExtraction.EmailTemplateType.Udemy
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

val EmailTemplateType.displayName: String
    get() = when (this) {
        is RakutenPay -> "Rakuten Pay"
        is AmazonKindle -> "Amazon Kindle"
        is AmazonItem -> "Amazon Item"
        is AmazonSubscribe -> "Amazon Subscribe"
        is ShikokuElectricPower -> "Shikoku Electric Power"
        is Udemy -> "Udemy"
        is RakutenCardETC -> "Rakuten Card ETC"
        is RakutenCardAll -> "Rakuten Card All"
    }

private val lastExecFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")

private fun formatLastExec(lastExecMillis: Long?): String {
    if (lastExecMillis == null) return "Not yet"
    return Instant.ofEpochMilli(lastExecMillis).atZone(ZoneId.systemDefault())
        .format(lastExecFormatter)
}

@Composable
fun MailboxExtractionScreenRoot(
    navHostController: NavHostController,
    viewModel: MailboxExtractionViewModel = viewModel(factory = MailboxExtractionViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
            navHostController.navigate(MainGraph.Global.ICategoryAssignment.Screen)
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
        if (uiState.isLoading || uiState.isWaitingForAuth) {
            LoadingBox(modifier = Modifier.padding(innerPadding))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GmailConnectionCard(
                isConnected = uiState.isGmailConnected,
                onConnectClick = onGmailConnectClick
            )

            if (uiState.isGmailConnected) {
                Text(
                    text = "Mail templates",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                uiState.emailTemplateTypeList.forEach { typeUiState ->
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = typeUiState.type.displayName,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "Last run: ${formatLastExec(typeUiState.lastExecMillis)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (typeUiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .padding(end = 12.dp)
                                            .size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                                Switch(
                                    checked = typeUiState.type.enabled,
                                    onCheckedChange = {
                                        onSwitchClick(typeUiState)
                                    },
                                    enabled = !typeUiState.isLoading
                                )
                            }

                            if (typeUiState.type is HasCategoryId<*>) {
                                CategoryDropDown(
                                    selectedCategoryId = typeUiState.type.categoryId,
                                    nullOption = true,
                                    categories = uiState.categories,
                                    onCategoryIdSelected = { categoryId ->
                                        onCategorySelect(
                                            typeUiState,
                                            categoryId
                                        )
                                    },
                                    enabled = !typeUiState.isLoading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp, bottom = 8.dp)
                                )
                            } else {
                                TextButton(
                                    modifier = Modifier.align(Alignment.End),
                                    onClick = {
                                        onCategoryAssignmentClick()
                                    }
                                ) {
                                    Text("Category Assignment")
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Default.OpenInNew,
                                        contentDescription = "Go to Category Assignment",
                                        modifier = Modifier
                                            .padding(start = 4.dp)
                                            .size(18.dp)
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

@Composable
private fun GmailConnectionCard(
    isConnected: Boolean,
    onConnectClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = if (isConnected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isConnected) Icons.Outlined.CheckCircle else Icons.Outlined.Email,
                contentDescription = null,
                tint = if (isConnected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Gmail",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (isConnected) "Connected" else "Not connected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isConnected) {
                OutlinedButton(onClick = onConnectClick) {
                    Text("Reconnect")
                }
            } else {
                Button(onClick = onConnectClick) {
                    Text("Connect")
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
                isLoading = true,
                lastExecMillis = 1_760_000_000_000
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
