package gaku.original.myapplication.ui.view.settings.menu.MailAutoExtraction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import gaku.original.myapplication.data.dataClass.Category
import gaku.original.myapplication.data.dataClass.MailAutoExtraction
import gaku.original.myapplication.ui.common.TopBarView
import gaku.original.myapplication.ui.common.enabledTextFiledColorSet
import gaku.original.myapplication.utility.LogAkitaDebug
import gaku.original.myapplication.viewModel.settings.MailAutoExtractionViewModel

@Composable
fun RakutenPaySettingView(
    navController: NavController,
    viewModel: MailAutoExtractionViewModel = hiltViewModel()
) {
    var rakutenPaySetting by remember {
        mutableStateOf(
            MailAutoExtraction.RakutenPay(
                shopCategoryAssignments =
                mapOf(
                    "shop1" to "category1",
                    "shop2" to "category2"
                )
            )
        )
    }

    var editedFlag by remember { mutableStateOf(false) }

    val
    LaunchedEffect(Unit) {
        viewModel.fetchCategories { }
    }

    Scaffold(
        topBar = {
            TopBarView(
                title = "楽天Pay",
                showBackButton = true,
                onBackNavClicked = {
                    if (editedFlag) {
                        /* 保存しなくて良いかポップアップをだす */
                    } else {
                        /* そのまま */
                        navController.popBackStack()
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {

            Text("とりあえずはメール抽出できた店だけにする")
            Text("将来的に自由に追加できるように")

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
                    checked = rakutenPaySetting.enabled,
                    onCheckedChange = {
                        rakutenPaySetting = rakutenPaySetting.copy(enabled = it)
                    }
                )
            }


            Button(
                onClick = {

                }
            ) {
                Text("この設定を保存")
            }

            if (rakutenPaySetting.shopCategoryAssignments != null)
                rakutenPaySetting.shopCategoryAssignments?.forEach { (shopName, categoryId) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = shopName,
                            modifier = Modifier.weight(1f),
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        CategoryDropDown(
                            initialCategoryId = categoryId,
                            categories = listOf(
                                Category("category1", 111, "aaa"),
                                Category("category2", 112, "bb")
                            ),
                            onCategorySelected = { category ->
                                val updatedMap = rakutenPaySetting.shopCategoryAssignments.orEmpty()
                                    .toMutableMap()
                                updatedMap[shopName] = category.id ?: "error"
                                rakutenPaySetting = rakutenPaySetting.copy(
                                    shopCategoryAssignments = updatedMap
                                )
                                editedFlag = true
                                LogAkitaDebug("${rakutenPaySetting.shopCategoryAssignments}")
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            else Text("履歴なし")
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

/***************楽天設定UI*******************/
@Composable
fun RakutenPaySettingColumn(
    rakutenPaySetting: MailAutoExtraction.RakutenPay,
    categories: List<Category>,
    onSave: (MailAutoExtraction.RakutenPay) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    var tmpRakutenPay by remember { mutableStateOf(rakutenPaySetting) }
    var newShopName by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    Column {
        /* 表示部分 */
        tmpRakutenPay.shopCategoryAssignments?.forEach { (shopName, categoryId) ->
            Row {
                TextField(
                    modifier = Modifier.weight(1f),
                    value = shopName,
                    onValueChange = {},
                    readOnly = true
                )
                Spacer(modifier = Modifier.width(5.dp))
                TextField(
                    modifier = Modifier.weight(1f),
                    value = categories.find { it.id == categoryId }?.name ?: "不明なカテゴリ",
                    onValueChange = {},
                    readOnly = true
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = newShopName ?: "",
                onValueChange = { newShopName = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        "店名", color = MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.5f
                        )
                    )
                },
            )
            Spacer(modifier = Modifier.width(20.dp))

        }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Button(
            colors = ButtonDefaults.buttonColors().copy(
                contentColor = MaterialTheme.colorScheme.onTertiary,
                containerColor = MaterialTheme.colorScheme.tertiary
            ),
            onClick = {
                if (newShopName != null && newShopName!!.isNotEmpty() && selectedCategory != null) {
                    val updatedMap =
                        tmpRakutenPay.shopCategoryAssignments.orEmpty().toMutableMap()
                    updatedMap[newShopName!!] = selectedCategory?.id ?: "aa"
                    tmpRakutenPay = tmpRakutenPay.copy(shopCategoryAssignments = updatedMap)
                    newShopName = ""
                    selectedCategory = null
                }
            }
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Update")
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            modifier = Modifier.width(100.dp),
            onClick = { onDismiss() },
            colors = ButtonDefaults.buttonColors()
                .copy(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Cancel")
        }
        Button(
            modifier = Modifier.width(100.dp),
            onClick = { onSave(tmpRakutenPay) }
        ) {
            Text("Save")
        }
    }
}