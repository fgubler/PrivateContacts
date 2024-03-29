/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.dialogs

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.R

@Composable
fun YesNoDialog(
    @StringRes title: Int,
    @StringRes text: Int,
    onYes: () -> Unit,
    onNo: () -> Unit,
) = YesNoDialog(
    title = title,
    text = { Text(text = stringResource(id = text)) },
    onYes = onYes,
    onNo = onNo,
)

@Composable
fun YesNoDialog(
    @StringRes title: Int,
    text: @Composable () -> Unit,
    yesButtonEnabled: Boolean = true,
    @StringRes yesButtonLabel: Int = R.string.yes,
    @StringRes noButtonLabel: Int = R.string.no,
    onYes: () -> Unit,
    onNo: () -> Unit,
) {
    AlertDialog(
        title = { Text(stringResource(id = title)) },
        text = text,
        onDismissRequest = onNo,
        confirmButton = {
            Button(onClick = onYes, enabled = yesButtonEnabled) {
                Text(stringResource(id = yesButtonLabel))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onNo) {
                Text(stringResource(id = noButtonLabel))
            }
        },
    )

    BackHandler { onNo() }
}
