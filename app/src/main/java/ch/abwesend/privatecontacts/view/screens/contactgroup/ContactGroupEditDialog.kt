/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.contactgroup

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contactgroup.IContactGroup
import ch.abwesend.privatecontacts.view.components.dialogs.YesNoDialog

@Composable
fun ContactGroupEditDialog(
    @StringRes title: Int,
    originalGroup: IContactGroup,
    allGroups: Collection<IContactGroup>,
    onSave: (IContactGroup) -> Unit,
    onCancel: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    var contactGroup: IContactGroup by remember { mutableStateOf(originalGroup) }
    var showDeleteConfirmation: Boolean by remember { mutableStateOf(false) }

    // the edited group itself is excluded to allow changing only the casing of its name
    val otherGroups = allGroups.filterNot { it.id.name == originalGroup.id.name }

    val groupValidity = when {
        contactGroup.id.name.isBlank() -> ContactGroupValidity.EMPTY_NAME
        otherGroups.any {
            it.id.name.equals(contactGroup.id.name, ignoreCase = true)
        } -> ContactGroupValidity.DUPLICATE_NAME
        else -> ContactGroupValidity.VALID
    }

    AlertDialog(
        title = { Text(stringResource(id = title)) },
        text = { ContactGroupEditComponent(contactGroup, groupValidity) { contactGroup = it } },
        onDismissRequest = onCancel,
        confirmButton = {
            Button(
                onClick = { if (contactGroup == originalGroup) onCancel() else onSave(contactGroup) },
                enabled = groupValidity == ContactGroupValidity.VALID,
            ) {
                Text(stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            Row {
                onDelete?.let {
                    TextButton(onClick = { showDeleteConfirmation = true }) {
                        Text(text = stringResource(id = R.string.delete), color = MaterialTheme.colorScheme.error)
                    }
                }
                OutlinedButton(onClick = onCancel) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        },
    )

    BackHandler { onCancel() }

    onDelete?.takeIf { showDeleteConfirmation }?.let { delete ->
        YesNoDialog(
            title = R.string.delete_contact_group_title,
            text = { Text(stringResource(id = R.string.delete_contact_group_text, originalGroup.id.name)) },
            onYes = {
                showDeleteConfirmation = false
                delete()
            },
            onNo = { showDeleteConfirmation = false },
        )
    }
}
