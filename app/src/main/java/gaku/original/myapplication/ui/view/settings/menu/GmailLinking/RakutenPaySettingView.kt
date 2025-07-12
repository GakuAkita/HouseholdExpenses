package gaku.original.myapplication.ui.view.settings.menu.GmailLinking

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import gaku.original.myapplication.data.Constants.AssignmentCondition
import gaku.original.myapplication.data.Constants.Status.CheckStatus
import gaku.original.myapplication.data.Constants.Status.SuspendFuncStatus
import gaku.original.myapplication.data.SuspendFuncStatusInfo
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.CategoryAssignment
import gaku.original.myapplication.data.dataClass.MailboxExtractionType
import gaku.original.myapplication.data.dataClass.checkAssignment
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

    var editedFlag by remember { mutableStateOf(false) }

    val allCategories by viewModel.categories.collectAsState(initial = emptyList())
    val mailboxSetting by viewModel.mailboxExtractionSetting.collectAsState(null)
    val rakutenPaySetting = mailboxSetting as? MailboxExtractionType.RakutenPay

    var editedAssignment = remember { mutableStateOf<CategoryAssignment?>(null) }

    var categoryFetchExec by rememberSaveable { mutableStateOf(false) }

    /* Parcelableじゃないとクラッシュする */
    /* 現在設定の取得結果を補完。.dataに設定の中身が入っている */
    var initialFetchSettingStatus by rememberSaveable { mutableStateOf<SuspendFuncStatusInfo?>(null) }

    var showPopBackConfirmDialog by remember {
        mutableStateOf(false)
    }

    var showCategoryAssignmentDialog by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current

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
            viewModel.fetchMailboxExtractionInternalSetting(/* 関数内でrakutenPaySettingを更新する */
                MailboxExtractionType.RakutenPay(),
                callback = {
                    initialFetchSettingStatus = it
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
            } else if (status.status != SuspendFuncStatus.SUCCESS || rakutenPaySetting == null) {
                /* 取り込み失敗 */
                /* 場合によってはnavigateでもとに戻った方が良い？？ */
                Text(
                    "現在設定の取得に失敗しました。\n" +
                            "message:${status.errorMessage}"
                )
            } else {
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
                        checked = rakutenPaySetting.enabled,//確実に入るはず、
                        onCheckedChange = { checked ->
                            rakutenPaySetting.let { current ->
                                val updated = current.copy(enabled = checked)
                                /*  */
                                viewModel.setMailboxExtractionInternalSetting(//内部でUI表示用の値を更新(成功のときのみ)
                                    updated,
                                    callback = {
                                        if (it.status != SuspendFuncStatus.SUCCESS) {
                                            val enabledStr = if (checked) "有効" else "無効"
                                            /* 失敗したとsnackBarを出す */
                                            scope.launch {
                                                snackBarHostState.currentSnackbarData?.dismiss()
                                                snackBarHostState.showSnackbar(
                                                    "${enabledStr}化に失敗しました",
                                                    actionLabel = "OK"
                                                )
                                            }
                                            LogAkitaDebug("")
                                        }
                                    }
                                )
                            }
                        }
                    )
                }

                Button(
                    onClick = {
                        editedAssignment.value = null//nullのときは新規追加
                        showCategoryAssignmentDialog = true
                    }
                ) {
                    Text("新しいカテゴリー割当を作成")
                }

                if (rakutenPaySetting.storeCategoryAssignments != null) {
                    rakutenPaySetting.storeCategoryAssignments.forEach { (id, assignment) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    /**
                                     *  editedAssignmentに値をあててダイアログを表示
                                     *  editedAssignmentはDialogの引数になっている
                                     *  */
                                    editedAssignment.value = assignment
                                    showCategoryAssignmentDialog = true
                                }
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                .padding(horizontal = 10.dp, vertical = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${assignment.name}",
                                modifier = Modifier.weight(1f),
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            /* カテゴリーIDからカテゴリーを表示する */
                            val hitCategory = allCategories.find { it.id == assignment.categoryId }
                            Text(
                                text = "${hitCategory?.name}",
                                modifier = Modifier.weight(1f),
                                fontSize = 20.sp
                            )
                        }
                    }
                }
            }

        }
    }

    /**
     * 新規追加と編集に対応したい。
     */
    if (showCategoryAssignmentDialog) {
        CategoryAssignmentDialog(
            initialAssignment = editedAssignment.value,
            onDismiss = { showCategoryAssignmentDialog = false },
            categories = allCategories,
            onSave = { assignment ->
                /* まあほぼないが、rakutenPaySettingがnullのときにここに可能性もある */
                if (rakutenPaySetting == null) {
                    return@CategoryAssignmentDialog
                }

                val result = checkAssignment(
                    assignment,
                    rakutenPaySetting.storeCategoryAssignments
                )

                if (result.status != CheckStatus.OK) {
                    Toast.makeText(context, result.errorMessage, Toast.LENGTH_SHORT).show()
                    return@CategoryAssignmentDialog
                }

                if (assignment.id == null) {
                    LogAkitaDebug("$assignment")
                    /* 新規追加のときはidを取ってこないとだめだな */
                    viewModel.addCategoryAssignment(
                        type = rakutenPaySetting,
                        assignment = assignment,
                    ) {
                        if (it.status == SuspendFuncStatus.SUCCESS) {
                            showCategoryAssignmentDialog = false
//                            scope.launch {
//                                snackBarHostState.currentSnackbarData?.dismiss()
//                                snackBarHostState.showSnackbar(
//                                    "新しい割当を追加しました",
//                                    actionLabel = "OK"
//                                )
//                            }
                        } else {
                            Toast.makeText(
                                context,
                                "追加失敗:${it.errorMessage}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {

                    val id = assignment.id

                    val currentAssignments = rakutenPaySetting.storeCategoryAssignments.orEmpty()

                    if (!currentAssignments.containsKey(id)) {
                        // エラー：存在しないIDを編集しようとしている
                        Toast.makeText(
                            context,
                            "指定された割当が見つかりませんでした",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@CategoryAssignmentDialog
                    }

                    // 更新処理
                    val updatedAssignments = currentAssignments.toMutableMap()
                    updatedAssignments[id] = assignment

                    val updatedSetting = rakutenPaySetting.copy(
                        storeCategoryAssignments = updatedAssignments
                    )

                    LogAkitaDebug("${updatedSetting}")
                    viewModel.setMailboxExtractionInternalSetting(updatedSetting) {
                        if (it.status == SuspendFuncStatus.SUCCESS) {
                            showCategoryAssignmentDialog = false

                            scope.launch {
                                snackBarHostState.currentSnackbarData?.dismiss()
                                snackBarHostState.showSnackbar(
                                    "割当を更新しました",
                                    actionLabel = "OK"
                                )
                            }
                        } else {
                            // エラー：存在しないIDを編集しようとしている
                            Toast.makeText(
                                context,
                                "更新失敗:${it.errorMessage}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                /* editedAssignmentがnullじゃないなら編集(update) */
            }
        )
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
                .fillMaxWidth()
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

@Composable
fun AssignmentConditionDropdown(
    initialCondition: String = "",
    onConditionSelected: (String) -> Unit,
    modifier: Modifier
) {
    var selectedCondition by remember { mutableStateOf(initialCondition) }
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
    ) {
        TextField(
            value = selectedCondition,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            colors = enabledTextFiledColorSet().copy(
                MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    expanded = !expanded
                }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(text = AssignmentCondition.CONTAINS) },
                onClick = {
                    selectedCondition = AssignmentCondition.CONTAINS
                    expanded = false
                    onConditionSelected(selectedCondition)
                }
            )

            DropdownMenuItem(
                text = { Text(text = AssignmentCondition.EXACT_MATCH) },
                onClick = {
                    selectedCondition = AssignmentCondition.EXACT_MATCH
                    expanded = false
                    onConditionSelected(selectedCondition)
                }
            )
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryAssignmentDialog(
    onSave: (CategoryAssignment) -> Unit = {},
    onDismiss: () -> Unit = {},
    initialAssignment: CategoryAssignment?,
    categories: List<Category>
) {
    // null のときはデフォルト値で初期化
    var assignment by remember {
        mutableStateOf(
            initialAssignment ?: CategoryAssignment(
                name = "",
                categoryId = null,
                condition = AssignmentCondition.EXACT_MATCH
            )
        )
    }

    BasicAlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        modifier = Modifier.background(color = MaterialTheme.colorScheme.onTertiary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                if (initialAssignment == null) {
                    Text("新しい割当を追加")
                } else {
                    Text("カテゴリー割当を編集")
                }
            }
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                value = assignment.name ?: "",
                onValueChange = {
                    assignment = assignment.copy(
                        name = it
                    )
                },
                placeholder = {
                    Text("店の名前")
                }
            )
            AssignmentConditionDropdown(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                initialCondition = assignment.condition ?: "",
                onConditionSelected = {
                    assignment = assignment.copy(
                        condition = it
                    )
                },
            )
            Text(
                "※完全一致:${AssignmentCondition.EXACT_MATCH} 部分一致:${AssignmentCondition.CONTAINS}",
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(5.dp))
            CategoryDropDown(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                initialCategoryId = assignment.categoryId,
                categories = categories,
                onCategorySelected = {
                    assignment = assignment.copy(
                        categoryId = it.id
                    )
                },
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        onDismiss()
                    }
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        onSave(assignment)
                    }
                ) {
                    Text("Save")
                }
            }
        }
    }
}

///***************楽天設定UI*******************/
//@Composable
//fun RakutenPaySettingColumn(
//    rakutenPaySetting: MailboxExtraction.RakutenPay,
//    categories: List<Category>,
//    onSave: (MailboxExtraction.RakutenPay) -> Unit = {},
//    onDismiss: () -> Unit = {}
//) {
//    var tmpRakutenPay by remember { mutableStateOf(rakutenPaySetting) }
//    var newShopName by remember { mutableStateOf<String?>(null) }
//    var expanded by remember { mutableStateOf(false) }
//    var selectedCategory by remember { mutableStateOf<Category?>(null) }
//
//    Column {
//        /* 表示部分 */
//        tmpRakutenPay.storeCategoryAssignments?.forEach { (shopName, categoryId) ->
//            Row {
//                TextField(
//                    modifier = Modifier.weight(1f),
//                    value = shopName,
//                    onValueChange = {},
//                    readOnly = true
//                )
//                Spacer(modifier = Modifier.width(5.dp))
//                TextField(
//                    modifier = Modifier.weight(1f),
//                    value = categories.find { it.id == categoryId }?.name ?: "不明なカテゴリ",
//                    onValueChange = {},
//                    readOnly = true
//                )
//            }
//        }
//
//        Row(
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            TextField(
//                value = newShopName ?: "",
//                onValueChange = { newShopName = it },
//                modifier = Modifier.weight(1f),
//                placeholder = {
//                    Text(
//                        "店名", color = MaterialTheme.colorScheme.onSurface.copy(
//                            alpha = 0.5f
//                        )
//                    )
//                },
//            )
//            Spacer(modifier = Modifier.width(20.dp))
//
//        }
//    }
//
//    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
//        Button(
//            colors = ButtonDefaults.buttonColors().copy(
//                contentColor = MaterialTheme.colorScheme.onTertiary,
//                containerColor = MaterialTheme.colorScheme.tertiary
//            ),
//            onClick = {
//                if (newShopName != null && newShopName!!.isNotEmpty() && selectedCategory != null) {
//                    val updatedMap =
//                        tmpRakutenPay.storeCategoryAssignments.orEmpty().toMutableMap()
//                    updatedMap[newShopName!!] = selectedCategory?.id ?: "aa"
//                    tmpRakutenPay = tmpRakutenPay.copy(storeCategoryAssignments = updatedMap)
//                    newShopName = ""
//                    selectedCategory = null
//                }
//            }
//        ) {
//            Icon(imageVector = Icons.Default.Add, contentDescription = "Update")
//        }
//    }
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(30.dp),
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        Button(
//            modifier = Modifier.width(100.dp),
//            onClick = { onDismiss() },
//            colors = ButtonDefaults.buttonColors()
//                .copy(containerColor = MaterialTheme.colorScheme.secondary)
//        ) {
//            Text("Cancel")
//        }
//        Button(
//            modifier = Modifier.width(100.dp),
//            onClick = { onSave(tmpRakutenPay) }
//        ) {
//            Text("Save")
//        }
//    }
//}