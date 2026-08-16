package gaku.original.myapplication.ui.screens.global.settingMenu.repeatAdd

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.MainGraph
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.RepeatAdd
import gaku.original.myapplication.ui.common.SwipeToRevealItem
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.ui.screens.global.settingMenu.repeatAdd.editDialog.toDisplayName
import timber.log.Timber
import java.time.LocalDateTime


@Composable
fun RepeatAddScreenRoot(
    viewModel: RepeatAddViewModel = viewModel(factory = RepeatAddViewModel.Factory),
    navHostController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = LocalSnackBarHostState.current

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    RepeatAddScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackNavClick = {
            navHostController.popBackStack()
        },
        onRepeatAddEdit = { it ->
            navHostController.navigate(MainGraph.SettingMenu.IRepeatAdd.Dialog(it))
        },
        onRepeatAddDelete = {
            Timber.d("Delete tapped??")
            viewModel.onDeleteClick(it)
        },
        onRepeatAddAddClick = {
            navHostController.navigate(MainGraph.SettingMenu.IRepeatAdd.Dialog(null))
        }
    )
}

@Composable
fun RepeatAddScreen(
    uiState: RepeatAddUiState,
    snackbarHostState: SnackbarHostState,
    onBackNavClick: () -> Unit,
    onRepeatAddEdit: (RepeatAdd) -> Unit,
    onRepeatAddDelete: (RepeatAdd) -> Unit,
    onRepeatAddAddClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopBarView(
                title = "Repeat Add",
                onBackNavClicked = onBackNavClick,
                showBackButton = true
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
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {
                Text("毎月1日に自動で追加されます")
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {
                Text("These expenses are automatically added on the first day of each month.")
            }
            Text("Swipe to delete.")

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                LazyColumn() {
                    itemsIndexed(
                        uiState.repeatAdds,
                        /* if key is not assigned, not properly recomposed. */
                        /* when deleted, the strange thing happens. */
                        key = { _, repeatAdd -> repeatAdd.id ?: "${LocalDateTime.now()}" }
                    ) { index, repeatAdd ->
                        RepeatAddItem(
                            repeatAdd = repeatAdd,
                            onEdit = {
                                onRepeatAddEdit(repeatAdd)
                            },
                            onDelete = {
                                onRepeatAddDelete(repeatAdd)
                            }
                        )
                    }
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 20.dp),
                    onClick = {
                        onRepeatAddAddClick()
                    }
                ) {
                    Text("Add RepeatAdd")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RepeatAddScreenPreview() {
    val uiState = RepeatAddUiState(
        repeatAdds = listOf(
            RepeatAdd(
                id = "1",
                expense = Expense(
                    amount = 200L
                )
            )
        )
    )

    RepeatAddScreen(
        uiState,
        snackbarHostState = SnackbarHostState(),
        onBackNavClick = {},
        onRepeatAddEdit = {},
        onRepeatAddDelete = {},
        onRepeatAddAddClick = {}
    )
}

@Composable
fun RepeatAddItem(repeatAdd: RepeatAdd, onEdit: () -> Unit = {}, onDelete: () -> Unit = {}) {
    val fontSize = 20.sp

    /* スクリーンの横幅を取得する */
    val configuration = LocalConfiguration.current
    val screenWidthPx = configuration.screenWidthDp.dp
    val density = LocalDensity.current
    val horizontalMaxOffset = with(density) { screenWidthPx.toPx() * 0.2f }

    SwipeToRevealItem(
        horizontalMaxOffset = -horizontalMaxOffset,
        backgroundColor = MaterialTheme.colorScheme.error,
        hiddenContent = {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(with(density) { (horizontalMaxOffset).toDp() })/* 引き出し領域の中央にDeleteアイコンを配置 */,
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        },
        onClick = {
            onDelete()
        },
    ) {
        Row(
            modifier = Modifier
                .border(width = 1.dp, color = MaterialTheme.colorScheme.onSecondary)
                .clickable {
                    onEdit()
                }
                .padding(vertical = 5.dp, horizontal = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${repeatAdd.frequencyInfo?.toDisplayName()}",
                modifier = Modifier.weight(1f),
                fontSize = fontSize,
                textAlign = TextAlign.Left//左寄せ
            )
            Text(
                text = "${repeatAdd.expense.amount}",
                modifier = Modifier.weight(1f),
                fontSize = fontSize,
                textAlign = TextAlign.Left,//左寄せ
            )
            Text(
                text = "${repeatAdd.expense.category?.name}",
                modifier = Modifier.weight(1f),
                fontSize = 20.sp,
                textAlign = TextAlign.Left
            )
        }
    }
}