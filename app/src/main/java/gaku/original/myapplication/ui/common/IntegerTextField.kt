package gaku.original.myapplication.ui.common

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

/**
 * If needed in the future, DecimalTextField should be created.
 */
@Composable
fun IntegerTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    /* below are parameters with the default value */
    label: @Composable (() -> Unit)? = null,
    isError: Boolean = false
) {
    TextField(
        modifier = modifier,
        value = value,
        onValueChange = { newValue ->
            if(newValue.isEmpty() || newValue.all { it.isDigit() }) {
                onValueChange(newValue)
            }
        },
        label = label,
        isError = isError,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        )
    )
}
