/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.dialogs

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import ch.abwesend.privatecontacts.R

@Composable
fun PasswordInputDialog(
    @StringRes title: Int,
    @StringRes label: Int,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit,
) {
    var value: String by remember { mutableStateOf("") }
    var passwordVisible: Boolean by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    BackHandler { onCancel() }

    AlertDialog(
        title = { Text(stringResource(id = title)) },
        text = {
            val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
            @StringRes val iconDescriptionRes = if (passwordVisible) R.string.hide_password else R.string.show_password
            val transformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()

            OutlinedTextField(
                label = { Text(stringResource(id = label)) },
                value = value,
                onValueChange = { value = it },
                visualTransformation = transformation,
                trailingIcon = {
                    val description = stringResource(id = iconDescriptionRes)
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = icon, contentDescription = description)
                    }
                },
                modifier = Modifier.focusRequester(focusRequester),
            )
        },
        onDismissRequest = onCancel,
        confirmButton = {
            Button(
                onClick = { onConfirm(value) },
                enabled = value.isNotEmpty(),
                content = { Text(stringResource(id = R.string.ok)) },
            )
        },
        dismissButton = {
            Button(onClick = onCancel) {
                Text(stringResource(id = R.string.cancel))
            }
        },
    )
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}
