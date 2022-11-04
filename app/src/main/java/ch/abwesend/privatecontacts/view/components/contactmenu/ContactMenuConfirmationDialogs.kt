package ch.abwesend.privatecontacts.view.components.contactmenu

import androidx.annotation.StringRes
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.view.components.dialogs.YesNoDialog

@Composable
fun MakeContactSecretConfirmationDialog(
    contacts: Set<IContactBase>,
    visible: Boolean,
    hideDialog: (changeContactType: Boolean) -> Unit,
) {
    if (visible) {
        @StringRes val titleRes = if (contacts.size == 1) R.string.make_contact_secret_title
        else R.string.make_contacts_secret_title

        YesNoDialog(
            title = titleRes,
            text = R.string.make_contact_secret_text,
            onYes = { hideDialog(true) },
            onNo = { hideDialog(false) },
        )
    }
}

@Composable
fun DeleteContactConfirmationDialog(
    contacts: Set<IContactBase>,
    visible: Boolean,
    hideDialog: (delete: Boolean) -> Unit,
) {
    if (visible) {
        @StringRes val titleRes = if (contacts.size == 1) R.string.delete_contact_title
        else R.string.delete_contacts_title
        val text = if (contacts.size == 1) stringResource(id = R.string.delete_contact_text)
        else stringResource(id = R.string.delete_contacts_text, contacts.size)

        YesNoDialog(
            title = titleRes,
            text = { Text(text = text) },
            onYes = { hideDialog(true) },
            onNo = { hideDialog(false) },
        )
    }
}
