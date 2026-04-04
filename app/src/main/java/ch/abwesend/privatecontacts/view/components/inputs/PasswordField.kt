package ch.abwesend.privatecontacts.view.components.inputs

import androidx.annotation.StringRes
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import ch.abwesend.privatecontacts.R

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes label: Int,
    modifier: Modifier = Modifier,
    onKeyboardDone: (() -> Unit)? = null,
) {
    var passwordVisible: Boolean by remember { mutableStateOf(false) }

    val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility

    @StringRes val iconDescriptionRes = if (passwordVisible) R.string.hide_password else R.string.show_password
    val transformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()

    OutlinedTextField(
        label = { Text(stringResource(id = label)) },
        value = value,
        onValueChange = onValueChange,
        visualTransformation = transformation,
        singleLine = true,
        trailingIcon = {
            val description = stringResource(id = iconDescriptionRes)
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = icon, contentDescription = description)
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = onKeyboardDone?.let { ImeAction.Done } ?: ImeAction.Default),
        keyboardActions = KeyboardActions(onDone = onKeyboardDone?.let { action -> { action() } }),
        modifier = modifier,
    )
}
