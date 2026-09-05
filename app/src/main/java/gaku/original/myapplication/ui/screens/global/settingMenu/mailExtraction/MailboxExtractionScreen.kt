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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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

@Composable
fun MailboxExtractionView(
    navController: NavController,
    viewModel: MailboxExtractionViewModel = hiltViewModel()
) {
//    val context = LocalContext.current
//
//    val loading by viewModel.loading.collectAsState(false)
//
//    val rakutenPaySettingState by viewModel.rakutenPaySettingState.collectAsState()
//    val amazonKindleSettingState by viewModel.amazonKindleSettingState.collectAsState()
//    val amazonItemSettingState by viewModel.amazonItemSettingState.collectAsState()
//    val amazonSubscribeState by viewModel.amazonSubscribeState.collectAsState()
//    val shikokuElectricSettingState by viewModel.shikokuElectricPowerSettingState.collectAsState()
//    val udemySettingState by viewModel.udemySettingState.collectAsState()
//    val rakutenCardETCSettingState by viewModel.rakutenCardETCSettingState.collectAsState()
//
//    val allCategories by viewModel.allCategories.collectAsState()
//    val isGmailTokenExist by viewModel.isGmailTokenExist.collectAsState()
//    val lastExecMap by viewModel.lastExecMap.collectAsState()
//
//    val lifecycleOwner = LocalLifecycleOwner.current
//
//    DisposableEffect(lifecycleOwner) {
//        val observer = LifecycleEventObserver { _, event ->
//            if (event == Lifecycle.Event.ON_RESUME) {
//                // ✅ ここに復帰時の処理を書く
//                println("App resumed!")
//                viewModel.loadIsGmailTokenExistWithLocalUpdate()
//            }
//        }
//
//        val lifecycle = lifecycleOwner.lifecycle
//        lifecycle.addObserver(observer)
//
//        // Composableが破棄されたときにobserverを削除
//        onDispose {
//            lifecycle.removeObserver(observer)
//        }
//    }
//
//    LaunchedEffect(Unit) {
//        viewModel.startInit()
//    }
//    val scope = rememberCoroutineScope()
//    val snackBarHostState = remember { SnackbarHostState() }
//
//    @Composable
//    fun MailboxExtractionMenu(
//        settingState: EmailTemplateSettingState,
//        onClick: () -> Unit = {}
//    ) {
//        val isSettingNull = settingState.setting == null
//        /**
//         * nullだったら何かしらのエラーが出ている。おそらく初期化に失敗。
//         */
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(5.dp)
//                .border(width = 1.dp, color = MaterialTheme.colorScheme.primary)
//                .clickable { onClick() }
//        ) {
//            Row(
//                modifier = Modifier,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = settingState.type.menuName,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .weight(1f)
//                        .padding(horizontal = 5.dp),
//                )
//
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .weight(1f),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    if (!isSettingNull) {
//                        if (settingState.setting?.enabled != null) {
//                            Text("有効化:")
//                            Switch(
//                                checked = settingState.setting.enabled,
//                                onCheckedChange = { checked ->
//                                    val updateSetting =
//                                        settingState.setting.copyWith(
//                                            enabled = checked
//                                        )
//                                    viewModel.updateEmailTemplateSettingWithLocalUpdate(
//                                        settingState.copy(
//                                            setting = updateSetting,
//                                        ),
//                                        callback = {
//                                            if (it.status != FuncStatus.SUCCESS) {
//                                                snackBarHostState.currentSnackbarData?.dismiss()
//                                                scope.launch {
//                                                    snackBarHostState.showSnackbar(
//                                                        message = it.errorMessage,
//                                                        actionLabel = "OK"
//                                                    )
//                                                }
//                                            }
//                                        }
//                                    )
//                                }
//                            )
//                        } else {
//                            /* ここに来ることはないはず */
//                            Text("Something went wrong. Contact the developer")
//                        }
//                    } else {
//                        /* 再ロードボタンを用意したほうがいいか？ */
//                        Text("初期化に失敗しています。。またはバグです。")
//                    }
//                }
//            }
//            Row(
//                modifier = Modifier
//                    //.padding(horizontal = 5.dp, vertical = 2.dp)
//                    .fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    "カテゴリー:", modifier = Modifier
//                        .fillMaxWidth()
//                        .weight(1f)
//                )
//                if (!isSettingNull &&
//                    settingState.setting is HasCategoryId
//                ) {
//                    CategoryDropDown(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .weight(2f),
//                        initialCategoryId = settingState.setting.categoryId,
//                        categories = allCategories,
//                        onCategorySelected = { category ->
//                            /**
//                             * firestoreでsetならいけるけど、updateだったらnullは無理？
//                             */
//                            val updatedSetting = settingState.setting.copyWith(
//                                categoryId = category.id
//                            )
//                            viewModel.updateEmailTemplateSettingWithLocalUpdate(
//                                //更新したやつ
//                                settingState.copy(
//                                    setting = updatedSetting,
//                                ),
//                                callback = {
//                                    if (it.status != FuncStatus.SUCCESS) {
//                                        snackBarHostState.currentSnackbarData?.dismiss()
//                                        scope.launch {
//                                            snackBarHostState.showSnackbar(
//                                                message = it.errorMessage,
//                                                actionLabel = "OK"
//                                            )
//                                        }
//                                    }
//                                }
//                            )
//                        },
//                        nullOption = true
//                    )
//                } else {
//                    TextButton(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .weight(2f),
//                        onClick = {
//                            /* カテゴリー割当画面へ */
//                            //navController.navigate(Screen.GlobalScreen.CategoryAssignmentEdit.route)
//                        }
//                    ) {
//                        Text("カテゴリー割当画面へ")
//                        Icon(
//                            imageVector = Icons.AutoMirrored.Default.OpenInNew,
//                            contentDescription = "Go to Category Assignment"
//                        )
//                    }
//                }
//            }
//
//            if (settingState.setting?.nodeName == EmailTemplateType.AmazonSubscribe().nodeName) {
//                /**
//                 *  AmazonSubscribeの場合は有効化&定期便の名前がヒットしないと検知できないようになっている。
//                 *  現状、メールの履歴だけで定期便かどうかを判断するのは難しいから。
//                 *  */
//
//            }
//
//            Row {
//                val timestamp = lastExecMap[settingState.type.nodeName]?.timestamp
//                var execTimeStr: String? = null
//                if (timestamp != null && timestamp > 0L) {
//                    val isoStr =
//                        TODO()
//                    execTimeStr = isoStr?.substringBefore('.') ?: "時刻取得失敗"
//                }
//                Text("最終実行時間 : ${if (execTimeStr == null) "未実行" else execTimeStr}")
//            }
//        }
//    }
//
//    fun openOAuthPage(context: Context, url: String) {
//        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//        context.startActivity(intent)
//    }
//
//    /******* UI ******/
//    Scaffold(
//        topBar = {
//            TopBarView(
//                title = "メールボックス自動連携",
//                showBackButton = true,
//                onBackNavClicked = {
//                    navController.popBackStack()
//                }
//            )
//        },
//        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
//    ) { innerPadding ->
//        Column(
//            modifier = Modifier
//                .padding(innerPadding)
//                .verticalScroll(rememberScrollState()),
//            verticalArrangement = Arrangement.Top
//        ) {
//            if (loading) {
//                Row(
//                    modifier = Modifier.fillMaxSize(),
//                    horizontalArrangement = Arrangement.Center,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
//                }
//            } else {
//                if (!isGmailTokenExist) {
//                    Text("現状、連携できるGmailはログインしているGmailと同じものだけです。")
//                    Text("たとえば 、a@gmail.comでこのアプリにログインしているのであれば、下のボタンのリンク先でa@gmail.comを選択してください。")
//                }
//                Row(
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    /**
//                     * ここでtokenがすでに存在していれば、
//                     * このGmailAPI許可ボタンは消す
//                     */
//                    Button(onClick = {
//                        viewModel.getOAuthUrl(
//                            callback = { status, url ->
//                                if (status.status == FuncStatus.SUCCESS) {
//                                    LogAkitaDebug("generated URL:$url")
//                                    openOAuthPage(context, url)
//                                } else {
//                                    snackBarHostState.currentSnackbarData?.dismiss()
//                                    scope.launch {
//                                        snackBarHostState.showSnackbar(
//                                            message = status.errorMessage,
//                                            actionLabel = "OK"
//                                        )
//                                    }
//                                }
//                            }
//                        )
//                    }) {
//                        if (isGmailTokenExist) {
//                            Text("Gmail API許可(すでに実施済み)")
//                        } else {
//                            Text("Gmail API許可")
//                        }
//                    }
//                }
//
//                if (isGmailTokenExist) {
//                    MailboxExtractionMenu(
//                        rakutenPaySettingState,
//                    )
//
//                    MailboxExtractionMenu(
//                        amazonKindleSettingState,
//                    )
//
//                    MailboxExtractionMenu(
//                        amazonItemSettingState,
//                    )
//
//                    MailboxExtractionMenu(
//                        amazonSubscribeState,
//                    )
//
//                    MailboxExtractionMenu(
//                        shikokuElectricSettingState,
//                    )
//
//                    MailboxExtractionMenu(
//                        udemySettingState,
//                    )
//
//                    MailboxExtractionMenu(
//                        rakutenCardETCSettingState,
//                    )
//                }
//            }
//        }
//    }
}