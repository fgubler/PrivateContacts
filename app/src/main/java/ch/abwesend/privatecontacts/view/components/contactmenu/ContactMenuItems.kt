package ch.abwesend.privatecontacts.view.components.contactmenu

import androidx.annotation.StringRes
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactBaseWithAccountInformation
import ch.abwesend.privatecontacts.view.model.ContactTypeChangeMenuConfig

@Composable
fun ChangeContactTypeMenuItem(
    contacts: Set<IContactBaseWithAccountInformation>,
    config: ContactTypeChangeMenuConfig,
    enabled: Boolean,
    onCloseMenu: (changeContactType: Boolean) -> Unit,
) {
    @StringRes val labelRes = if (contacts.size == 1) config.menuTextSingularRes
    else config.menuTextPluralRes

    var confirmationDialogVisible: Boolean by remember { mutableStateOf(false) }

    DropdownMenuItem(enabled = enabled, onClick = { confirmationDialogVisible = true }) {
        Text(stringResource(id = labelRes))
    }

    ChangeContactTypeConfirmationDialog(
        contacts = contacts,
        visible = confirmationDialogVisible,
        config = config,
        hideDialog = { changeContacts ->
            confirmationDialogVisible = false
            onCloseMenu(changeContacts)
        },
    )
}

@Composable
fun DeleteContactMenuItem(
    contacts: Set<IContactBase>,
    onCloseMenu: (delete: Boolean) -> Unit,
) {
    var deleteConfirmationDialogVisible: Boolean by remember { mutableStateOf(false) }
    val multipleContacts = contacts.size > 1

    DropdownMenuItem(onClick = { deleteConfirmationDialogVisible = true }) {
        @StringRes val text = if (multipleContacts) R.string.delete_contacts else R.string.delete_contact
        Text(stringResource(id = text))
    }

    DeleteContactConfirmationDialog(
        contacts = contacts,
        visible = deleteConfirmationDialogVisible,
        hideDialog = { delete ->
            deleteConfirmationDialogVisible = false
            onCloseMenu(delete)
        },
    )
}
