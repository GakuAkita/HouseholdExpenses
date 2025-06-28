package gaku.original.myapplication.ui.view.settings.menu.GmailLinking

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.MailboxExtraction
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.ui.common.enabledTextFiledColorSet
import gaku.original.myapplication.utility.LogAkitaDebug
import gaku.original.myapplication.viewModel.settings.MailboxExtractionViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RakutenPaySettingView(
    navController: NavController,
    viewModel: MailboxExtractionViewModel = hiltViewModel()
) {
    val funcName = "RakutenPaySettingView"

    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    var rakutenPaySetting by remember {
        mutableStateOf<MailboxExtraction.RakutenPay?>(
            null
        )
    }

    var editedFlag by remember { mutableStateOf(false) }

    val allCategories by viewModel.categories.collectAsState(initial = emptyList())

    var categoryFetchExec by rememberSaveable { mutableStateOf(false) }

    /* Parcelableじゃないとクラッシュする */
    var initialFetchSettingStatus by rememberSaveable { mutableStateOf<SuspendFuncStatusInfo?>(null) }

    var showPopBackConfirmDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        if (!categoryFetchExec) {
            viewModel.fetchCategories {
                if (it.status == SuspendFuncStatus.SUCCESS) {
                    /* カテゴリーは失敗したら何度も実行してよいが、初期設定はだめ */
                    categoryFetchExec = true
                } else {
                    /* カテゴリー取得に失敗したらスナックバーに出す */
                    Log.d(funcName, "Unable to fetch Categories!!")
                }
            }
        }

        if (initialFetchSettingStatus == null) {
            viewModel.fetchMailboxExtractionInternalSetting(
                MailboxExtraction.RakutenPay(),
                callback = {
                    initialFetchSettingStatus = it.toSuspendFuncStatusInfo()
                    if (it.status == SuspendFuncStatus.SUCCESS) {
                        if (it.data != null) {
                            rakutenPaySetting = it.data as MailboxExtraction.RakutenPay
                        }/* dataがnullのときは未設定のとき */
                    }
                }
            )
        }
    }

    // 戻るをハンドル（Backボタン & スワイプ戻り含む）
    BackHandler(enabled = true) {
        if (editedFlag) {
            showPopBackConfirmDialog = true
        } else {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopBarView(
                title = "楽天Pay",
                showBackButton = true,
                onBackNavClicked = {
                    /* そのまま */
                    if (editedFlag) {
                        showPopBackConfirmDialog = true
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            /* 一回変数にあてないとだめっぽい。savableをつかっていると。 */
            val status = initialFetchSettingStatus
            if (status == null) {
                /* ローディング中なはず、、 */
                CircularProgressIndicator()
            } else if (status.status != SuspendFuncStatus.SUCCESS) {
                /* 取り込み失敗 */
                /* 場合によってはnavigateでもとに戻った方が良い？？ */
                Text(
                    "現在設定の取得に失敗しました。\n" +
                            "message:${status.errorMessage}"
                )
            } else {
                /* savableだとこのように変数に当てないとだめっぽい？ */
                val internalRakutenPaySetting =
                    rakutenPaySetting ?: MailboxExtraction.RakutenPay()/* nullなはずがない */
                Text("とりあえずはメール抽出できた店だけにする")
                Text("将来的に自由に追加できるように")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("楽天Pay設定ON/OFF")
                    Switch(
                        modifier = Modifier
                            .scale(scaleX = 1.5f, scaleY = 1.2f)
                            .padding(horizontal = 30.dp),
                        checked = internalRakutenPaySetting.enabled,
                        onCheckedChange = {
                            rakutenPaySetting = internalRakutenPaySetting.copy(enabled = it)
                        }
                    )
                }

                Button(
                    onClick = {
                        viewModel.setMailboxExtractionInternalSetting(
                            internalRakutenPaySetting,
                            callback = {
                                if (it.status == SuspendFuncStatus.SUCCESS) {
                                    /* 保存に成功しました */
                                    editedFlag = false
                                    viewModel.setMailboxExtractionInternalSetting(
                                        internalRakutenPaySetting,
                                        callback = {})
                                } else {
                                    /* 失敗しました */
                                }
                            }
                        )
                    }
                ) {
                    Text("この設定を保存")
                }

                if (internalRakutenPaySetting.shopCategoryAssignments != null)
                    internalRakutenPaySetting.shopCategoryAssignments.forEach { (shopName, categoryId) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = shopName,
                                modifier = Modifier.weight(1f),
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            CategoryDropDown(
                                initialCategoryId = categoryId,
                                categories = allCategories,
                                onCategorySelected = { category ->
                                    val updatedMap =
                                        internalRakutenPaySetting.shopCategoryAssignments.orEmpty()
                                            .toMutableMap()
                                    updatedMap[shopName] = category.id ?: "error"
                                    rakutenPaySetting = internalRakutenPaySetting.copy(
                                        shopCategoryAssignments = updatedMap
                                    )
                                    editedFlag = true
                                    LogAkitaDebug("${internalRakutenPaySetting.shopCategoryAssignments}")
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                else Text("履歴なし")
            }

        }
    }

    if (showPopBackConfirmDialog) {
        BasicAlertDialog(
            onDismissRequest = {
                showPopBackConfirmDialog = false
            },
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("編集中の設定は破棄されます。 よろしいですか？")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            showPopBackConfirmDialog = false
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.buttonColors().copy(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text(
                            "変更を破棄",
                            modifier = Modifier
                                .padding(horizontal = 10.dp),
                        )
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                snackBarHostState.currentSnackbarData?.dismiss()
                                snackBarHostState.showSnackbar(
                                    "保存ボタンを押してください",
                                    actionLabel = "OK"
                                )
                            }
                            showPopBackConfirmDialog = false
                        }
                    ) {
                        Text("キャンセル")
                    }
                }
            }

        }

    }
}

@Composable
fun CategoryDropDown(
    initialCategoryId: String?,
    categories: List<Category>,
    onCategorySelected: (Category) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    if (selectedCategory == null) {
        selectedCategory = categories.find { it.id == initialCategoryId }
    }

    Box(
        modifier = modifier
    ) {
        TextField(
            value = selectedCategory?.name ?: "カテゴリー選択",
            onValueChange = {},
            readOnly = true,
            enabled = false,
            colors = enabledTextFiledColorSet().copy(
                disabledTextColor = if (selectedCategory == null) MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.5f
                )
                else MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .clickable {
                    expanded = !expanded
                }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(text = category.name ?: "") },
                    onClick = {
                        selectedCategory = category
                        expanded = false
                        onCategorySelected(category)
                    }
                )
            }
        }
    }

}

/***************楽天設定UI*******************/
@Composable
fun RakutenPaySettingColumn(
    rakutenPaySetting: MailboxExtraction.RakutenPay,
    categories: List<Category>,
    onSave: (MailboxExtraction.RakutenPay) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    var tmpRakutenPay by remember { mutableStateOf(rakutenPaySetting) }
    var newShopName by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    Column {
        /* 表示部分 */
        tmpRakutenPay.shopCategoryAssignments?.forEach { (shopName, categoryId) ->
            Row {
                TextField(
                    modifier = Modifier.weight(1f),
                    value = shopName,
                    onValueChange = {},
                    readOnly = true
                )
                Spacer(modifier = Modifier.width(5.dp))
                TextField(
                    modifier = Modifier.weight(1f),
                    value = categories.find { it.id == categoryId }?.name ?: "不明なカテゴリ",
                    onValueChange = {},
                    readOnly = true
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = newShopName ?: "",
                onValueChange = { newShopName = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        "店名", color = MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.5f
                        )
                    )
                },
            )
            Spacer(modifier = Modifier.width(20.dp))

        }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Button(
            colors = ButtonDefaults.buttonColors().copy(
                contentColor = MaterialTheme.colorScheme.onTertiary,
                containerColor = MaterialTheme.colorScheme.tertiary
            ),
            onClick = {
                if (newShopName != null && newShopName!!.isNotEmpty() && selectedCategory != null) {
                    val updatedMap =
                        tmpRakutenPay.shopCategoryAssignments.orEmpty().toMutableMap()
                    updatedMap[newShopName!!] = selectedCategory?.id ?: "aa"
                    tmpRakutenPay = tmpRakutenPay.copy(shopCategoryAssignments = updatedMap)
                    newShopName = ""
                    selectedCategory = null
                }
            }
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Update")
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            modifier = Modifier.width(100.dp),
            onClick = { onDismiss() },
            colors = ButtonDefaults.buttonColors()
                .copy(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Cancel")
        }
        Button(
            modifier = Modifier.width(100.dp),
            onClick = { onSave(tmpRakutenPay) }
        ) {
            Text("Save")
        }
    }
}