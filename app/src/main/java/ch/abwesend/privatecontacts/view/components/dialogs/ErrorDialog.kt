/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.dialogs

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.R

@Composable
fun ErrorDialog(
    errorMessage: String,
    @StringRes title: Int = R.string.error,
    @StringRes okButtonLabel: Int = R.string.close,
    onClose: () -> Unit,
    onRetry: (() -> Unit)? = null,
) {
    if (onRetry == null) {
        OkDialog(title = title, okButtonLabel = okButtonLabel, onClose = onClose) {
            Text(text = errorMessage)
        }
    } else {
        AlertDialog(
            title = { Text(stringResource(id = title)) },
            text = { Text(text = errorMessage) },
            onDismissRequest = onClose,
            confirmButton = {
                Button(onClick = onRetry) {
                    Text(stringResource(id = R.string.try_again))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onClose) {
                    Text(stringResource(id = okButtonLabel))
                }
            },
        )

        BackHandler { onClose() }
    }
}
