/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.dialogs

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.view.components.inputs.DoNotShowAgainCheckbox

data class OkDialogTexts(
    @param:StringRes val title: Int,
    @param:StringRes val text: Int,
)

@Composable
fun OkDialog(
    texts: OkDialogTexts,
    @StringRes okButtonLabel: Int = R.string.ok,
    onClose: () -> Unit,
) = OkDialog(title = texts.title, text = texts.text, okButtonLabel = okButtonLabel, onClose = onClose)

@Composable
fun OkDialog(
    @StringRes title: Int,
    @StringRes text: Int,
    @StringRes okButtonLabel: Int = R.string.ok,
    onClose: () -> Unit,
) {
    AlertDialog(
        title = { Text(stringResource(id = title)) },
        text = { Text(stringResource(id = text)) },
        onDismissRequest = onClose,
        confirmButton = {
            Button(onClick = onClose) {
                Text(stringResource(id = okButtonLabel))
            }
        },
    )

    BackHandler { onClose() }
}

@Composable
fun OkDialog(
    @StringRes title: Int,
    onClose: () -> Unit,
    @StringRes okButtonLabel: Int = R.string.ok,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        title = { Text(stringResource(id = title)) },
        text = content,
        onDismissRequest = onClose,
        confirmButton = {
            Button(onClick = onClose) {
                Text(stringResource(id = okButtonLabel))
            }
        },
    )

    BackHandler { onClose() }
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
    )

    BackHandler { onClose(doNotShowAgain) }
}
