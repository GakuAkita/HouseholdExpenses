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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.sp
import gaku.original.myapplication.data.Interface.CategoryAssignNamePattern
import gaku.original.myapplication.data.dataClass.AssignmentCondition
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.CategoryAssignment

/**
 * 同じ名前のCategoryDropDownが定義されているが、
 * これはこれで問題ないらしい
 */
@Composable
fun CategoryDropDown(
    modifier: Modifier = Modifier.width(280.dp),
    initialCategory: Category?,
    categories: List<Category>,
    onCategorySelected: (Category) -> Unit,
    nullOption: Boolean = false,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    // 初期カテゴリーを設定
    LaunchedEffect(initialCategory, categories) {
        if (selectedCategory == null) {
            if (initialCategory != null) {
                // まず現在のカテゴリーリストから探す
                val foundCategory = categories.find { it.id == initialCategory.id }
                selectedCategory = foundCategory ?: initialCategory // 見つからない場合は元のカテゴリーを使用
            }
        }
    }

    LaunchedEffect(enabled) {
        if(!enabled){
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
                    if(enabled){
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
                        selectedCategory = Category(id = null, name = null)
                        expanded = false
                        onCategorySelected(Category(id = null, name = null))
                    }
                )
            }

            // 現在選択されているカテゴリーが削除されたカテゴリーの場合は表示
            if (selectedCategory != null &&
                selectedCategory !in categories &&
                selectedCategory?.name != null/* when nullOption is true, this exists in options which should be avoided. */
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = selectedCategory?.name ?: "不明なカテゴリー",
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        expanded = false
                        onCategorySelected(selectedCategory!!)
                    }
                )
            }

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
fun CategoryDropDown(
    initialCategoryId: String?,
    categories: List<Category>,
    onCategorySelected: (Category) -> Unit,
    nullOption: Boolean = false,
    modifier: Modifier = Modifier,
) {
    // initialCategoryIdをCategoryオブジェクトに変換
    val initialCategory = remember(initialCategoryId, categories) {
        if (initialCategoryId != null) {
            categories.find { it.id == initialCategoryId } ?: Category(
                id = initialCategoryId,
                name = "削除されたカテゴリー"
            )
        } else {
            null
        }
    }

    CategoryDropDown(
        initialCategory = initialCategory,
        categories = categories,
        onCategorySelected = onCategorySelected,
        nullOption = nullOption,
        modifier = modifier
    )
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

@Composable
fun CategoryAssignNamePatternDropdown(
    initialNamePattern: CategoryAssignNamePattern?,
    onPatternSelected: (CategoryAssignNamePattern) -> Unit,
    modifier: Modifier,
    enabled: Boolean = true
) {
    var selectedNamePattern by remember { mutableStateOf(initialNamePattern) }
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
    ) {
        TextField(
            value = selectedNamePattern?.label ?: "名前パターン選択",
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

        if (enabled) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(text = CategoryAssignNamePattern.STORE.label) },
                    onClick = {
                        selectedNamePattern = CategoryAssignNamePattern.STORE
                        expanded = false
                        onPatternSelected(selectedNamePattern!!)/* 気を付けて */
                    }
                )

                DropdownMenuItem(
                    text = { Text(text = CategoryAssignNamePattern.PRODUCT.label) },
                    onClick = {
                        selectedNamePattern = CategoryAssignNamePattern.PRODUCT
                        expanded = false
                        onPatternSelected(selectedNamePattern!!)
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryAssignmentDialog(
    titleContent: @Composable () -> Unit = {},
    onSave: (CategoryAssignment, CategoryAssignNamePattern) -> Unit = { _, _ -> },
    onDismiss: () -> Unit = {},
    initialAssignment: CategoryAssignment?,
    categories: List<Category>,
    initialNamePattern: CategoryAssignNamePattern? = null,//店名なのか、製品名なのか,
    isNamePatternSelectable: Boolean = false
) {
    var namePattern by remember { mutableStateOf(initialNamePattern) }

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CategoryAssignNamePatternDropdown(
                    initialNamePattern = namePattern,
                    onPatternSelected = {
                        namePattern = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    enabled = isNamePatternSelectable
                )
            }

            if (namePattern != null) {
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
                        if (initialNamePattern == null && !isNamePatternSelectable) {
                            /**
                             *  名前パターンが選択不可なのに名前パターンの初期値が入っていないのはバグ
                             *  */
                            Text("これはバグです。開発者に連絡してください")
                        } else {
                            /* これが表示されるときにnamePatternがnullであることはない。 */
                            Text("${namePattern?.label}を入力してください")
                        }
                    }
                )
            }

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
                        if (namePattern == null) {
                            // 名前パターンが選択されていない場合はエラーを表示する
                            // ここでは簡単にトーストなどで通知することを想定
                            return@Button
                        }
                        onSave(assignment, namePattern!!)
                    }
                ) {
                    Text("Save")
                }
            }
        }
    }
}
