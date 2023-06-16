/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.importexport

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.view.components.text.SectionTitle

object ImportExportScreenComponents {

    @Composable
    fun ImportExportCategory(@StringRes title: Int, content: @Composable () -> Unit) {
        Card(
            modifier = Modifier
                .padding(all = 5.dp)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                SectionTitle(titleRes = title, addTopPadding = false)
                content()
            }
        }
    }

    @Composable
    fun ImportExportSuccessDialog(
        @StringRes title: Int,
        @StringRes secondButtonText: Int,
        secondButtonVisible: Boolean,
        onClose: () -> Unit,
        onSecondButton: () -> Unit,
        content: @Composable () -> Unit,
    ) {
        val secondButton: @Composable (() -> Unit)? = if (secondButtonVisible) {
            { OutlinedButton(onClick = onSecondButton) { Text(stringResource(id = secondButtonText)) } }
        } else null

        AlertDialog(
            title = { Text(stringResource(id = title)) },
            text = content,
            onDismissRequest = onClose,
            confirmButton = {
                Button(onClick = onClose) {
                    Text(stringResource(id = R.string.close))
                }
            },
            dismissButton = secondButton,
        )

        BackHandler { onClose() }
    }
}
