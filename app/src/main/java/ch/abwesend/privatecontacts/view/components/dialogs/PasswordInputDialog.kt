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
import androidx.compose.material.Text
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
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.view.components.inputs.PasswordField

@Composable
fun PasswordInputDialog(
    @StringRes title: Int,
    @StringRes label: Int,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit,
) {
    var value: String by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    BackHandler { onCancel() }

    AlertDialog(
        title = { Text(stringResource(id = title)) },
        text = {
            PasswordField(
                value = value,
                onValueChange = { value = it },
                label = label,
                modifier = Modifier.focusRequester(focusRequester),
                onKeyboardDone = {
                    if (value.isNotEmpty()) {
                        onConfirm(value)
                    }
                },
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
