package gaku.original.myapplication.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun FloatingActionButtonWithIcon(
    onClick: () -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    FloatingActionButton(
        onClick = {
            onClick()
        },
        containerColor = containerColor,
        contentColor = contentColor
    ) {
        Icon(
            Icons.Filled.Add,
            contentDescription = "Add Button"
        )
    }
}
