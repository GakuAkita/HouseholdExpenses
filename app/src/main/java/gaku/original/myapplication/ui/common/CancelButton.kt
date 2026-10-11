package gaku.original.myapplication.ui.common

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/* Cancel is not destructive, so it is outlined instead of red. */
@Composable
fun CancelButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    OutlinedButton(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled
    ) {
        Text("Cancel")
    }
}
