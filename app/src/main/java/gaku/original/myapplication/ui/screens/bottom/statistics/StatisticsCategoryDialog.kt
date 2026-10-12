package gaku.original.myapplication.ui.screens.bottom.statistics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import gaku.original.myapplication.MainGraph
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.Expense
import gaku.original.myapplication.data.dataClass.GeneratedType
import gaku.original.myapplication.ui.common.DialogCard
import gaku.original.myapplication.ui.common.toYen
import gaku.original.myapplication.ui.screens.global.expenseAddEdit.ExpenseAddEditMode
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dialogMonthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
private val expenseDateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)

@Composable
fun StatisticsCategoryDialogRoot(
    categoryId: String?,
    viewModel: StatisticsViewModel,
    navHostController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StatisticsCategoryDialog(
        month = uiState.selectedMonth,
        share = uiState.summary?.shares?.find { it.categoryId == categoryId },
        zoneId = uiState.zoneId,
        /* The dialog stays under the edit screen, so going back returns to it. */
        onExpenseClick = {
            navHostController.navigate(MainGraph.Global.ExpenseAddEdit(ExpenseAddEditMode.Edit(it)))
        },
        onClose = { navHostController.popBackStack() }
    )
}

/**
 * The largest expenses of a category in [month]. Tap one to edit it.
 * @param share null when the category has no expenses in the month, for example after a refresh.
 */
@Composable
fun StatisticsCategoryDialog(
    month: YearMonth,
    share: CategoryShare?,
    zoneId: ZoneId,
    onExpenseClick: (Expense) -> Unit,
    onClose: () -> Unit,
) {
    DialogCard(
        title = share?.name ?: "No expenses",
        actions = {
            TextButton(onClick = onClose) {
                Text("Close")
            }
        }
    ) {
        Text(
            text = month.format(dialogMonthFormatter),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (share == null) {
            Text(
                text = "This category has no expenses this month.",
                style = MaterialTheme.typography.bodyMedium
            )
            return@DialogCard
        }

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (share.count == 1) "1 expense" else "${share.count} expenses",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = share.amount.toYen(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Text(
            text = if (share.count > share.topExpenses.size) {
                "Largest ${share.topExpenses.size}"
            } else {
                "Largest first"
            },
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column {
            share.topExpenses.forEach { expense ->
                TopExpenseRow(expense = expense, zoneId = zoneId, onClick = { onExpenseClick(expense) })
            }
        }
    }
}

@Composable
private fun TopExpenseRow(expense: Expense, zoneId: ZoneId, onClick: () -> Unit) {
    val description = listOfNotNull(expense.storeName, expense.itemName, expense.note)
        .filter { it.isNotBlank() }
        .joinToString(" / ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = expense.datetime.atZone(zoneId).format(expenseDateFormatter),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(44.dp)
        )
        Text(
            text = description.ifEmpty { "No details" },
            style = MaterialTheme.typography.bodyMedium,
            color = if (description.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = expense.amount.toYen(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StatisticsCategoryDialogPreview() {
    val food = Category(id = "food", timestamp = 0L, name = "Food", enabled = true)
    fun expense(amount: Long, date: String, storeName: String?, itemName: String?) = Expense(
        id = date,
        generatedType = GeneratedType.Manual,
        datetime = Instant.parse("${date}T03:00:00Z"),
        timestamp = 0L,
        amount = amount,
        category = food,
        note = null,
        storeName = storeName,
        itemName = itemName,
    )

    StatisticsCategoryDialog(
        month = YearMonth.of(2026, 10),
        share = CategoryShare(
            categoryId = "food",
            name = "Food",
            amount = 72_000,
            ratio = 0.395f,
            count = 30,
            topExpenses = listOf(
                expense(12_800, "2026-10-04", "Costco", null),
                expense(8_400, "2026-10-18", "Sushi Zanmai", "Birthday dinner"),
                expense(5_200, "2026-10-09", null, null),
            )
        ),
        zoneId = ZoneId.of("Asia/Tokyo"),
        onExpenseClick = {},
        onClose = {}
    )
}
