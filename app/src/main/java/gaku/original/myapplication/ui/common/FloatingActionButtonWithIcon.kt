package gaku.original.myapplication.ui.common

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FloatingActionButtonWithIcon(
    onClick: () -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
    contentColor: Color = MaterialTheme.colorScheme.onSecondary
) {
    FloatingActionButton(
        onClick = {
            onClick()
        },
        containerColor = containerColor,
        contentColor = contentColor,
        shape = CircleShape,
        modifier = Modifier.size(80.dp),
        elevation = FloatingActionButtonDefaults.elevation(0.dp)//デフォルトだとElevationがついているっぽい。
    ) {
        Icon(
            Icons.Filled.Add,
            contentDescription = "Add Button",
            modifier = Modifier.size(36.dp)
        )
    }
}