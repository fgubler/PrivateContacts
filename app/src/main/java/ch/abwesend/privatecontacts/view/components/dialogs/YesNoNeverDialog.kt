/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.dialogs

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.view.components.inputs.DoNotShowAgainCheckbox

@Composable
fun YesNoNeverDialog(
    @StringRes title: Int,
    @StringRes text: Int,
    @StringRes secondaryTextBlock: Int?,
    onYes: () -> Unit,
    onNo: (doNotShowAgain: Boolean) -> Unit,
) {
    var doNotShowAgainState by remember { mutableStateOf(false) }

    AlertDialog(
        title = { Text(stringResource(id = title)) },
        text = {
            Column {
                Text(stringResource(id = text))
                secondaryTextBlock?.let {
                    Text(modifier = Modifier.padding(top = 10.dp), text = stringResource(id = it))
                }
                DoNotShowAgainCheckbox(checked = doNotShowAgainState) {
                    doNotShowAgainState = it
                }
            }
        },
        onDismissRequest = { onNo(doNotShowAgainState) },
        confirmButton = {
            Button(onClick = onYes) {
                Text(stringResource(id = R.string.yes))
            }
        },
        dismissButton = {
            Button(onClick = { onNo(doNotShowAgainState) }) {
                Text(stringResource(id = R.string.no))
            }
        },
    )
}
