package gaku.original.myapplication.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable

//enabled=falseにしても同じ色のスタイルを保持したい。色のセットを保存しておく
@Composable
fun enabledTextFiledColorSet() = TextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant, // 無効化時のインジケーター色を変更
    disabledTextColor = MaterialTheme.colorScheme.onSurface, // テキスト色を維持
    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
)