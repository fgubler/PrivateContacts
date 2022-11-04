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

@Composable
fun MakeContactSecretMenuItem(contacts: Set<IContactBase>, onCloseMenu: (changeContactType: Boolean) -> Unit) {
    @StringRes val labelRes = if (contacts.size == 1) R.string.make_contact_secret
    else R.string.make_contacts_secret

    var confirmationDialogVisible: Boolean by remember { mutableStateOf(false) }

    DropdownMenuItem(onClick = { confirmationDialogVisible = true }) {
        Text(stringResource(id = labelRes))
    }

    MakeContactSecretConfirmationDialog(
        contacts = contacts,
        visible = confirmationDialogVisible,
        hideDialog = { changeContacts ->
            confirmationDialogVisible = false
            onCloseMenu(changeContacts)
        },
    )
}
