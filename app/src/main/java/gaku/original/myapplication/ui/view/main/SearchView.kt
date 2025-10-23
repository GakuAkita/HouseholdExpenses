package gaku.original.myapplication.ui.view.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import gaku.original.myapplication.Screen
import gaku.original.myapplication.data.AppTimeZone
import gaku.original.myapplication.data.Constants.Status.FuncStatus
import gaku.original.myapplication.data.Constants.Status.LoadingStatus
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.ExpenseSearchFilter
import gaku.original.myapplication.data.dataClass.GeneratedType
import gaku.original.myapplication.data.dataClass.convertGeneratedTypeToDisplayName
import gaku.original.myapplication.ui.common.BottomBarView
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.utility.LogAkitaDebug
import gaku.original.myapplication.viewModel.main.SearchViewModel
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchView(
    viewModel: SearchViewModel = hiltViewModel(),
    navController: NavController
) {
    val expenses = viewModel.searchedExpenses.collectAsState()
    val listState = rememberLazyListState()
    val loadingStatus = viewModel.loadingStatus.collectAsState()
    val currentFilter = viewModel.currentFilter.collectAsState()
    
    var showFilterSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.searchExpenses {
            if (it.status == FuncStatus.SUCCESS) {
                LogAkitaDebug("search success!!")
            } else if (it.status == FuncStatus.TIMEOUT) {
                LogAkitaDebug("Timeout")
            } else {
                LogAkitaDebug("Failed")
            }
        }
    }

    Scaffold(
        topBar = {
            TopBarView("検索")
        },
        bottomBar = { BottomBarView(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // フィルターコントロール
            FilterControlBar(
                currentFilter = currentFilter.value,
                onShowFilterSheet = { showFilterSheet = true },
                onResetFilter = {
                    viewModel.resetFilter()
                    viewModel.searchExpenses {
                        LogAkitaDebug("Reset filter and search")
                    }
                },
                onRefresh = {
                    viewModel.searchExpenses {
                        LogAkitaDebug("Refresh search")
                    }
                }
            )

            // コンテンツ
            if (loadingStatus.value == LoadingStatus.LOADING) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (loadingStatus.value == LoadingStatus.ERROR) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("データの取得に失敗しました。")
                }
            } else if (loadingStatus.value == LoadingStatus.TIMEOUT) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("タイムアウトしました。")
                }
            } else if (loadingStatus.value == LoadingStatus.COMPLETED && expenses.value.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("検索結果がありません")
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxWidth(),
                    userScrollEnabled = true
                ) {
                    items(expenses.value) { expense ->
                        SearchedExpenseItem(expense) {
                            viewModel.setToTmpExpense(it)
                            navController.navigate(
                                Screen.GlobalScreen.ExpenseAddEdit.createRoute(
                                    Screen.SearchScreen
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // フィルターシート
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            FilterSheetContent(
                currentFilter = currentFilter.value,
                onApplyFilter = { filter ->
                    viewModel.searchWithFilter(filter) {
                        LogAkitaDebug("Apply filter and search")
                    }
                    showFilterSheet = false
                },
                onDismiss = { showFilterSheet = false }
            )
        }
    }
}

/**
 * フィルターコントロールバー
 */
@Composable
fun FilterControlBar(
    currentFilter: ExpenseSearchFilter,
    onShowFilterSheet: () -> Unit,
    onResetFilter: () -> Unit,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "フィルター",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "更新")
                    }
                    IconButton(onClick = onShowFilterSheet) {
                        Icon(Icons.Default.FilterList, contentDescription = "フィルター設定")
                    }
                }
            }
            
            // アクティブなフィルターを表示
            val activeCount = currentFilter.activeFilterCount()
            if (activeCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "適用中: ${activeCount}件",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onResetFilter,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("リセット", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

/**
 * フィルターシートの内容
 */
@Composable
fun FilterSheetContent(
    currentFilter: ExpenseSearchFilter,
    onApplyFilter: (ExpenseSearchFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var generatedTypes by remember { mutableStateOf(currentFilter.generatedTypes ?: emptyList()) }
    var categoryOnlyNull by remember { 
        mutableStateOf(
            currentFilter.categoryIds != null && 
            currentFilter.categoryIds.size == 1 && 
            currentFilter.categoryIds[0] == null
        ) 
    }
    var storeNameText by remember { mutableStateOf(currentFilter.storeName ?: "") }
    var itemNameText by remember { mutableStateOf(currentFilter.itemName ?: "") }
    var noteText by remember { mutableStateOf(currentFilter.note ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "検索フィルター",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "閉じる")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // GeneratedTypeフィルター
        Text(
            text = "生成タイプ",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        val availableTypes = listOf(
            GeneratedType.MANUAL to "手動生成",
            GeneratedType.AUTO to "自動生成",
            GeneratedType.REPEAT_ADD to "繰り返し追加",
            GeneratedType.MAIL_EXTRACTION to "メール抽出"
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableTypes.forEach { (type, label) ->
                FilterChip(
                    selected = type in generatedTypes,
                    onClick = {
                        generatedTypes = if (type in generatedTypes) {
                            generatedTypes - type
                        } else {
                            generatedTypes + type
                        }
                    },
                    label = { Text(label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // カテゴリーフィルター
        Text(
            text = "カテゴリー",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        FilterChip(
            selected = categoryOnlyNull,
            onClick = { categoryOnlyNull = !categoryOnlyNull },
            label = { Text("カテゴリー未設定のみ") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // テキスト検索フィルター
        Text(
            text = "テキスト検索",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = storeNameText,
            onValueChange = { storeNameText = it },
            label = { Text("ストア名") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = itemNameText,
            onValueChange = { itemNameText = it },
            label = { Text("アイテム名") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("メモ") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 適用ボタン
        Button(
            onClick = {
                val newFilter = ExpenseSearchFilter(
                    generatedTypes = if (generatedTypes.isNotEmpty()) generatedTypes else null,
                    categoryIds = if (categoryOnlyNull) listOf(null) else null,
                    storeName = storeNameText.ifBlank { null },
                    itemName = itemNameText.ifBlank { null },
                    note = noteText.ifBlank { null }
                )
                onApplyFilter(newFilter)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("フィルターを適用")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SearchedExpenseItem(
    expense: Expense,
    onClick: (Expense) -> Unit = {}
) {
    val fontSize = 18.sp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 50.dp) // 最小高さを指定
            .clickable { onClick(expense) }
            .padding(10.dp)
    ) {
        val localDateTime: LocalDateTime? = AppTimeZone.isoStringToLocalDateTime(expense.datetime)
        if (localDateTime == null) {
            Text(modifier = Modifier.weight(1f), text = "datetime error", fontSize = fontSize)
        } else {
            val year = localDateTime.year
            val month = localDateTime.monthValue
            val day = localDateTime.dayOfMonth
            Text("$year/$month/$day", modifier = Modifier.weight(1f), fontSize = fontSize)
        }
        Text("${expense.amount}円", modifier = Modifier.weight(1f), fontSize = fontSize)

        val generatedType: String? = expense.generatedType
        if (generatedType == null) {
            Text("タイプエラー", modifier = Modifier.weight(1f), fontSize = fontSize)
        } else {
            val (mainType, subType) = convertGeneratedTypeToDisplayName(generatedType)
            Text(
                if (subType != null) "${mainType}/${subType}" else mainType,
                modifier = Modifier.weight(1f),
                fontSize = fontSize
            )
        }
    }
}