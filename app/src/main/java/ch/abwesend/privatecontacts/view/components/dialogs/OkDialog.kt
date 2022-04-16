/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.dialogs

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.view.components.inputs.DoNotShowAgainCheckbox

@Composable
fun OkDialog(
    @StringRes title: Int,
    @StringRes text: Int,
    onClose: () -> Unit,
) {
    AlertDialog(
        title = { Text(stringResource(id = title)) },
        text = { Text(stringResource(id = text)) },
        onDismissRequest = onClose,
        confirmButton = {
            Button(onClick = onClose) {
                Text(stringResource(id = R.string.ok))
            }
        },
        dismissButton = {},
    )
}

@Composable
fun OkDialog(
    @StringRes title: Int,
    onClose: () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        title = { Text(stringResource(id = title)) },
        text = content,
        onDismissRequest = onClose,
        confirmButton = {
            Button(onClick = onClose) {
                Text(stringResource(id = R.string.ok))
            }
        },
        dismissButton = {},
    )
}

@Composable
fun OkDoNotShowAgainDialog(
    @StringRes title: Int,
    @StringRes text: Int,
    onClose: (doNotShowAgain: Boolean) -> Unit,
) {
    var doNotShowAgain by remember { mutableStateOf(false) }

    AlertDialog(
        title = { Text(stringResource(id = title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(stringResource(id = text))
                DoNotShowAgainCheckbox(checked = doNotShowAgain) {
                    doNotShowAgain = it
                }
            }
        },
        onDismissRequest = { onClose(doNotShowAgain) },
        confirmButton = {
            Button(onClick = { onClose(doNotShowAgain) }) {
                Text(stringResource(id = R.string.ok))
            }
        },
        dismissButton = {},
    )
}
