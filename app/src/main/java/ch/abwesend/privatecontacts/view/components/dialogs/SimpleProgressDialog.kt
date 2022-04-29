/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.dialogs

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.view.components.LoadingIndicatorFullWidth

@Composable
fun SimpleProgressDialog(
    @StringRes title: Int,
    allowRunningInBackground: Boolean,
    onClose: () -> Unit = {},
) {
    val onCloseDialog = if (allowRunningInBackground) onClose else ({ })

    Dialog(onDismissRequest = onCloseDialog) {
        Surface(shape = MaterialTheme.shapes.medium) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 20.dp, horizontal = 10.dp)
            ) {
                Text(text = stringResource(id = title))
                LoadingIndicatorFullWidth(
                    loadingIndicatorSize = 50.dp,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
                if (allowRunningInBackground) {
                    OutlinedButton(onClick = onCloseDialog) {
                        Text(text = stringResource(id = R.string.run_in_background))
                    }
                }
            }
        }

        BackHandler { onCloseDialog() }
    }
}
