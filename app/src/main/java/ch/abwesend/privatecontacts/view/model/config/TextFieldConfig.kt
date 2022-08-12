package ch.abwesend.privatecontacts.view.model.config

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp

data class TextFieldConfig(
    val keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    val maxLines: Int = 1,
    val minHeight: Dp = Dp.Unspecified,
) {
    val singleLine: Boolean
        get() = maxLines == 1

    constructor(keyboardType: KeyboardType) : this(
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType)
    )
}
