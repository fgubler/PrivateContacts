/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.dialogs

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.view.components.inputs.PasswordField

@Composable
fun PasswordInputDialog(
    @StringRes title: Int,
    @StringRes label: Int,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit,
    confirmationRequired: Boolean = false,
) {
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmationFocusRequester = remember { FocusRequester() }

    var value: String by remember { mutableStateOf("") }
    var confirmationValue: String by remember { mutableStateOf("") }

    val passwordsNotMatching = confirmationRequired &&
            value.isNotEmpty() &&
            confirmationValue.isNotEmpty() &&
            value != confirmationValue

    val isConfirmButtonEnabled = if (confirmationRequired) {
        value.isNotEmpty() && value == confirmationValue
    } else {
        value.isNotEmpty()
    }

    BackHandler { onCancel() }
    LaunchedEffect(Unit) { passwordFocusRequester.requestFocus() }

    AlertDialog(
        title = { Text(stringResource(id = title)) },
        text = {
            Column {
                PasswordField(
                    value = value,
                    onValueChange = { value = it },
                    label = label,
                    modifier = Modifier.focusRequester(passwordFocusRequester),
                    onKeyboardDone = {
                        if (value.isNotEmpty() && confirmationRequired) {
                            confirmationFocusRequester.requestFocus()
                        } else if (value.isNotEmpty()) {
                            onConfirm(value)
                        }
                    },
                )
                if (confirmationRequired) {
                    Spacer(modifier = Modifier.height(10.dp))
                    PasswordField(
                        value = confirmationValue,
                        onValueChange = { confirmationValue = it },
                        label = R.string.backup_encryption_password_confirmation_label,
                        modifier = Modifier.focusRequester(confirmationFocusRequester),
                        onKeyboardDone = {
                            if (isConfirmButtonEnabled) {
                                onConfirm(value)
                            }
                        },
                    )
                    if (passwordsNotMatching) {
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = stringResource(id = R.string.password_confirmation_mismatch_error),
                            color = MaterialTheme.colors.error,
                            style = MaterialTheme.typography.caption,
                        )
                    }
                }
            }
        },
        onDismissRequest = onCancel,
        confirmButton = {
            Button(
                onClick = { onConfirm(value) },
                enabled = isConfirmButtonEnabled,
                content = { Text(stringResource(id = R.string.ok)) },
            )
        },
        dismissButton = {
            Button(onClick = onCancel) {
                Text(stringResource(id = R.string.cancel))
            }
        },
    )
}
