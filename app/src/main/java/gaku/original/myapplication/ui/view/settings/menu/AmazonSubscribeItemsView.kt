package gaku.original.myapplication.ui.view.settings.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import gaku.original.myapplication.data.Constants.Status.LoadingStatus
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.viewModel.settings.AmazonSubscribeItemsViewModel

@Composable
fun AmazonSubscribeItemsView(
    viewModel: AmazonSubscribeItemsViewModel = hiltViewModel(),
    navController: NavController
) {
    // ViewModelの状態を監視
    val loadingStatus by viewModel.loadingStatus.collectAsState()
    val amazonSubscribeItems by viewModel.amazonSubscribeItems.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

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
                    if (amazonSubscribeItems.isEmpty()) {
                        Text("定期便アイテムはありません")
                    } else {
                        LazyColumn {
                            items(amazonSubscribeItems.toList()) { (_, item) ->
                                Text(
                                    text = "${item.productName ?: "(商品名不明)"} x${item.quantity ?: 1} / ¥${item.price ?: 0f}",
                                    modifier = Modifier.padding(vertical = 8.dp)
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
    }
}
