/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.dialogs

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R

@Composable
fun YesNoNeverDialog(
    @StringRes title: Int,
    @StringRes text: Int,
    onYes: () -> Unit,
    onNo: (doNotShowAgain: Boolean) -> Unit,
) {
    val doNotShowAgainState = remember { mutableStateOf(false) }

    AlertDialog(
        title = { Text(stringResource(id = title)) },
        text = {
            Column() {
                Text(stringResource(id = text))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .clickable { doNotShowAgainState.value = !doNotShowAgainState.value }
                ) {
                    Checkbox(
                        checked = doNotShowAgainState.value,
                        onCheckedChange = { } // click in the entire row triggers that already
                    )
                    Text(
                        text = stringResource(id = R.string.do_not_show_dialog_again),
                        modifier = Modifier.padding(start = 5.dp),
                    )
                }
            }
        },
        onDismissRequest = { onNo(doNotShowAgainState.value) },
        confirmButton = {
            Button(onClick = onYes) {
                Text(stringResource(id = R.string.yes))
            }
        },
        dismissButton = {
            Button(onClick = { onNo(doNotShowAgainState.value) }) {
                Text(stringResource(id = R.string.no))
            }
        },
    )
}
