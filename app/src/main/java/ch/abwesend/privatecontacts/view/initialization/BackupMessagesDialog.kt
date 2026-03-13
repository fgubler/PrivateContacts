/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.initialization

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.backup.BackupMessage
import ch.abwesend.privatecontacts.domain.model.backup.BackupMessageSeverity
import ch.abwesend.privatecontacts.view.components.dialogs.OkDialog

@Composable
fun BackupMessagesDialog(messages: List<BackupMessage>, onClose: () -> Unit) {
    OkDialog(
        title = R.string.backup_messages_dialog_title,
        onClose = onClose,
    ) {
        LazyColumn {
            items(messages) { message ->
                val fontStyle = when (message.severity) {
                    BackupMessageSeverity.WARNING -> FontStyle.Italic
                    BackupMessageSeverity.ERROR -> FontStyle.Normal
                }
                Text(
                    text = "• ${message.text}",
                    fontStyle = fontStyle,
                    style = MaterialTheme.typography.body2,
                )
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
    }
}
