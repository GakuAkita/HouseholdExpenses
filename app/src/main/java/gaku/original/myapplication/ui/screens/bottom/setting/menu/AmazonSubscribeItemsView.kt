package gaku.original.myapplication.ui.screens.bottom.setting.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import gaku.original.myapplication.data.Constants.Status.LoadingStatus
import gaku.original.myapplication.data.dataClass.AmazonSubscribeItem
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.viewModel.settings.AmazonSubscribeItemsViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmazonSubscribeItemsView(
    viewModel: AmazonSubscribeItemsViewModel = hiltViewModel(),
    navController: NavController
) {
    // ViewModelの状態を監視
    val loadingStatus by viewModel.loadingStatus.collectAsState()
    val amazonSubscribeItems by viewModel.amazonSubscribeItems.collectAsState()
    val disabledAmazonSubscribeItems by viewModel.disabledAmazonSubscribeItems.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    // 削除済みアイテムダイアログの状態
    var showDisabledItemsDialog by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // エラーが発生した場合の処理
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            // エラーメッセージを表示（Snackbarなど）
            // ここでエラーをクリアすることも可能
            // viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopBarView(
                "Amazon定期便アイテム",
                showBackButton = true,
                onBackNavClicked = { navController.popBackStack() }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (loadingStatus) {
                LoadingStatus.IDLE, LoadingStatus.LOADING -> {
                    // ローディング表示
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Text("Amazon定期便アイテムを読み込み中...")
                    }
                }

                LoadingStatus.SUCCESS -> {
                    // データ表示
                    if (amazonSubscribeItems.isEmpty() && disabledAmazonSubscribeItems.isEmpty()) {
                        // 空の状態の改善
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "定期便アイテムはありません",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "メールから自動的に追加されます",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 注意書き
                            item {
                                AmazonSubscribeItemsInfoCard()
                            }
                            
                            // 削除済みアイテム表示ボタン
                            if (disabledAmazonSubscribeItems.isNotEmpty()) {
                                item {
                                    DisabledItemsButton(
                                        disabledItemsCount = disabledAmazonSubscribeItems.size,
                                        onClick = { showDisabledItemsDialog = true }
                                    )
                                }
                            }
                            
                            // 有効なアイテムのみを表示
                            items(amazonSubscribeItems.toList()) { (_, item) ->
                                AmazonSubscribeItemCard(
                                    item = item,
                                    onDeleteClick = { viewModel.disableItem(it) }
                                )
                            }
                        }
                    }
                }

                LoadingStatus.ERROR -> {
                    // エラー表示
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("エラーが発生しました")
                        Text(
                            text = errorMessage ?: "不明なエラー",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Button(
                            onClick = { viewModel.refresh() }
                        ) {
                            Text("再読み込み")
                        }
                    }
                }

                LoadingStatus.TIMEOUT -> {
                    // タイムアウト表示
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("タイムアウトしました")
                        Text(
                            text = "ネットワーク接続を確認して再度お試しください",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Button(
                            onClick = { viewModel.refresh() }
                        ) {
                            Text("再読み込み")
                        }
                    }
                }

                else -> {
                    Text("不明な状態です")
                }
            }
        }
        
        // 削除済みアイテムダイアログ
        if (showDisabledItemsDialog) {
            ModalBottomSheet(
                onDismissRequest = { showDisabledItemsDialog = false },
                sheetState = bottomSheetState
            ) {
                DisabledItemsDialogContent(
                    disabledItems = disabledAmazonSubscribeItems,
                    onRestoreClick = { item ->
                        viewModel.enableItem(item)
                    },
                    onDismiss = { showDisabledItemsDialog = false }
                )
            }
        }
    }
}

@Composable
private fun AmazonSubscribeItemsInfoCard() {
    var isExpanded by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "検知方法について",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "折りたたむ" else "展開する",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Amazonから届く「次回の配達」メールから商品名を自動抽出して追加しています。定期便キャンセルメールが届いた場合は、ここから自動的に削除されます。したがって、自分でこのページを触る必要はありません。\n\nしかし、稀にキャンセルメールが届いても削除されないケースもあります。その場合は、各アイテムのゴミ箱アイコンから手動で削除してください。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = TextUnit(16f, TextUnitType.Sp)
                )
            }
        }
    }
}

@Composable
private fun AmazonSubscribeItemCard(
    item: AmazonSubscribeItem,
    onDeleteClick: (AmazonSubscribeItem) -> Unit
) {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale.JAPAN)
    val formattedPrice = numberFormat.format((item.price ?: 0f).toDouble())

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // アイコン
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 商品情報
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 商品名
                Text(
                    text = item.productName ?: "(商品名不明)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 数量と価格
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 数量
                    Text(
                        text = "数量: ${item.quantity ?: 1}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 価格
                    Text(
                        text = formattedPrice,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // 削除ボタン
            IconButton(
                onClick = {
                    onDeleteClick(item)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "削除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun DisabledItemsButton(
    disabledItemsCount: Int,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "削除済みアイテム",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${disabledItemsCount}件",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = "展開",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DisabledItemsDialogContent(
    disabledItems: Map<String, AmazonSubscribeItem>,
    onRestoreClick: (AmazonSubscribeItem) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // ヘッダー
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "削除済みアイテム (${disabledItems.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onDismiss) {
                Text("閉じる")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (disabledItems.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "削除済みアイテムはありません",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(disabledItems.toList()) { (_, item) ->
                    DisabledItemInDialog(
                        item = item,
                        onRestoreClick = { onRestoreClick(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DisabledItemInDialog(
    item: AmazonSubscribeItem,
    onRestoreClick: (AmazonSubscribeItem) -> Unit
) {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale.JAPAN)
    val formattedPrice = numberFormat.format((item.price ?: 0f).toDouble())

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // アイコン
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 商品情報
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 商品名
                Text(
                    text = item.productName ?: "(商品名不明)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 数量と価格
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 数量
                    Text(
                        text = "数量: ${item.quantity ?: 1}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )

                    // 価格
                    Text(
                        text = formattedPrice,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            // 復元ボタン
            IconButton(
                onClick = {
                    onRestoreClick(item)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "復元",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
