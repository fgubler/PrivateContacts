package ch.abwesend.privatecontacts.view.components.contactmenu

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
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
import ch.abwesend.privatecontacts.view.components.dialogs.YesNoDialog
import ch.abwesend.privatecontacts.view.model.ContactTypeChangeMenuConfig

@Composable
fun ChangeContactTypeConfirmationDialog(
    contacts: Set<IContactBaseWithAccountInformation>,
    visible: Boolean,
    config: ContactTypeChangeMenuConfig,
    hideDialog: (changeContactType: Boolean) -> Unit,
) {
    if (visible) {
        @StringRes val titleRes = if (contacts.size == 1) config.confirmationDialogTitleSingularRes
        else config.confirmationDialogTitlePluralRes
        var saveButtonEnabled by remember { mutableStateOf(true) }

        YesNoDialog(
            title = titleRes,
            text = {
                Column {
                    Text(text = stringResource(id = config.confirmationDialogTextRes))
                    config.ConfirmationDialogAdditionalContent(contacts = contacts) { enabled ->
                        saveButtonEnabled = enabled
                    }
                }
            },
            yesButtonEnabled = saveButtonEnabled,
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
