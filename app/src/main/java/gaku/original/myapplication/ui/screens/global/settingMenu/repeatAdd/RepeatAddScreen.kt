package gaku.original.myapplication.ui.screens.global.settingMenu.repeatAdd

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import gaku.original.myapplication.LocalSnackBarHostState
import gaku.original.myapplication.MainGraph
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.ExpenseTemplate
import gaku.original.myapplication.data.dataClass.RepeatAdd
import gaku.original.myapplication.data.dataClass.RepeatFrequency
import gaku.original.myapplication.ui.common.EmptyState
import gaku.original.myapplication.ui.common.LoadingBox
import gaku.original.myapplication.ui.common.SwipeToRevealItem
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.ui.common.toYen
import gaku.original.myapplication.ui.screens.global.settingMenu.repeatAdd.editDialog.toDetailText
import timber.log.Timber
import java.time.LocalDateTime


@Composable
fun RepeatAddScreenRoot(
    viewModel: RepeatAddViewModel = viewModel(factory = RepeatAddViewModel.Factory),
    navHostController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
        onRepeatAddEdit = {
            Timber.d("${it}")
            navHostController.navigate(MainGraph.SettingMenu.IRepeatAdd.EditDialog(it))
        },
        onRepeatAddDelete = {
            Timber.d("Delete tapped??")
            viewModel.onDeleteClick(it)
        },
        onRepeatAddAddClick = {
            navHostController.navigate(MainGraph.SettingMenu.IRepeatAdd.EditDialog(null))
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
        },
        floatingActionButton = {
            if (!uiState.isLoading) {
                ExtendedFloatingActionButton(
                    onClick = {
                        onRepeatAddAddClick()
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Add RepeatAdd") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            LoadingBox(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    RepeatAddInfoCard()
                }

                if (uiState.repeatAdds.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Outlined.Repeat,
                            title = "No repeat adds yet",
                            message = "Add rent, subscriptions and other regular expenses here."
                        )
                    }
                }

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
        }
    }
}

@Composable
private fun RepeatAddInfoCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "毎月1日に自動で追加されます",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    "These expenses are automatically added on the first day of each month.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    "Tap to edit. Swipe left to delete.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
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
                timestamp = 0L,
                expense = ExpenseTemplate(
                    amount = 200L,
                    category = Category(id = "食費", timestamp = 0L, name = "食費", enabled = true)
                ),
                frequencyInfo = RepeatFrequency.EveryMonth(day = 25, hour = 9, minute = 0)
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
    /* スクリーンの横幅を取得する */
    val configuration = LocalConfiguration.current
    val screenWidthPx = configuration.screenWidthDp.dp
    val density = LocalDensity.current
    val horizontalMaxOffset = with(density) { screenWidthPx.toPx() * 0.2f }

    SwipeToRevealItem(
        modifier = Modifier.clip(MaterialTheme.shapes.medium),
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
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .clickable {
                    onEdit()
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Repeat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = repeatAdd.expense.category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = repeatAdd.frequencyInfo.toDetailText(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = repeatAdd.expense.amount.toYen(),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
