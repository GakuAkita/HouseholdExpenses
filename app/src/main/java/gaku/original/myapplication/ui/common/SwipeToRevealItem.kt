package gaku.original.myapplication.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset

@Composable
fun SwipeToRevealItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    hiddenContent: @Composable () -> Unit = {},
    backgroundColor: Color = MaterialTheme.colorScheme.error,
    horizontalMaxOffset: Float = -300f,
    item: @Composable () -> Unit
) {
    val maxOffsetX = horizontalMaxOffset // 最大スワイプ量（px）
    var offsetX by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // 背景
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable {
                    onClick()
                }
                .background(backgroundColor),
            contentAlignment = Alignment.CenterEnd
        ) {
            hiddenContent()
        }

        // スワイプ可能な前面アイテム
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.toInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        val newOffset = (offsetX + delta).coerceIn(maxOffsetX, 0f)
                        offsetX = newOffset
                    },
                    onDragStopped = {
                        // 一定距離スワイプしたら開いたままにする
                        if (offsetX < maxOffsetX / 2) {
                            offsetX = maxOffsetX
                        } else {
                            offsetX = 0f
                        }
                    }
                )
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)

        ) {
            item()
        }
    }
}
