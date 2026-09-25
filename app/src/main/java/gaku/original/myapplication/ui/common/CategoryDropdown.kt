package gaku.original.myapplication.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import gaku.original.myapplication.data.dataClass.Category

/**
 * 同じ名前のCategoryDropDownが定義されているが、
 * これはこれで問題ないらしい
 */
@Composable
fun CategoryDropDown(
    modifier: Modifier = Modifier.width(280.dp),
    selectedCategory: Category?,
    categories: List<Category>,
    onCategorySelected: (Category) -> Unit,
    nullOption: Boolean = false,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(enabled) {
        if (!enabled) {
            expanded = false
        }
    }

    Box(
        modifier = modifier
    ) {
        TextField(
            value = selectedCategory?.name ?: "Select Category",
            onValueChange = {},
            readOnly = true,
            enabled = false,
            colors = enabledTextFiledColorSet().copy(
                disabledTextColor = if (selectedCategory == null) MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.8f
                )
                else MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (enabled) {
                        expanded = !expanded
                    }
                }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (nullOption) {
                DropdownMenuItem(
                    text = { Text(text = "(null)", color = MaterialTheme.colorScheme.tertiary) },
                    onClick = {
                        expanded = false
                        onCategorySelected(Category(id = null, name = null))
                    }
                )
            }

            // 現在選択されているカテゴリーが削除されたカテゴリーの場合は表示
            if (selectedCategory != null &&
                selectedCategory !in categories &&
                selectedCategory.name != null/* when nullOption is true, this exists in options which should be avoided. */
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = selectedCategory.name,
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        expanded = false
                        onCategorySelected(selectedCategory)
                    }
                )
            }

            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(text = category.name ?: "") },
                    onClick = {
                        expanded = false
                        onCategorySelected(category)
                    }
                )
            }
        }
    }
}

/* こっちはCategoryIdだけ渡せば良い。Category本体でなくても */
@Composable
fun CategoryDropDown(
    modifier: Modifier = Modifier,
    selectedCategoryId: String?,
    categories: List<Category>,
    onCategorySelected: (Category) -> Unit,
    nullOption: Boolean = false,
    enabled: Boolean = true
) {
    // initialCategoryIdをCategoryオブジェクトに変換
    val selectedCategory =
        if (selectedCategoryId != null) {
            categories.find { it.id == selectedCategoryId } ?: Category(
                id = selectedCategoryId,
                name = "削除されたカテゴリー"
            )
        } else {
            null
        }

    CategoryDropDown(
        selectedCategory = selectedCategory,
        categories = categories,
        onCategorySelected = onCategorySelected,
        nullOption = nullOption,
        modifier = modifier,
        enabled = enabled
    )
}