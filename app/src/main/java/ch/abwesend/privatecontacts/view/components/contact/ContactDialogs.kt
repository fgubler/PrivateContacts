/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.contact

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.view.components.dialogs.OkDialog

@Composable
fun DeleteContactsErrorDialog(
    numberOfErrors: Int,
    multipleContacts: Boolean = false,
    onClose: () -> Unit
) {
    if (numberOfErrors > 0) {
        OkDialog(
            title = R.string.error,
            onClose = onClose
        ) {
            @StringRes
            val descriptionResource =
                if (multipleContacts) R.string.delete_contacts_error
                else R.string.delete_contact_error

            Text(text = stringResource(id = descriptionResource))
        }

        BackHandler { onClose() }
    }
}
