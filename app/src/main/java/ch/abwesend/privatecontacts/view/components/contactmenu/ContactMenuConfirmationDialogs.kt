package ch.abwesend.privatecontacts.view.components.contactmenu

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
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
            onYes = {
                hideDialog(true)
            },
            onNo = { hideDialog(false) }
        )
    }
}
