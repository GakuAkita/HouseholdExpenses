package gaku.original.myapplication.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gaku.original.myapplication.data.dataClass.AssignmentCondition
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.CategoryAssignment

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
    titleContent: @Composable () -> Unit = {},
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                titleContent()
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
