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
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError
import ch.abwesend.privatecontacts.view.components.dialogs.OkDialog

@Composable
fun DeleteContactsErrorDialog(
    errors: List<ContactChangeError>,
    multipleContacts: Boolean = false,
    onClose: () -> Unit
) {
    if (errors.isNotEmpty()) {
        OkDialog(
            title = R.string.error,
            onClose = onClose
        ) {
            @StringRes
            val descriptionResource =
                if (multipleContacts) R.string.delete_contacts_error
                else R.string.delete_contact_error

            val description = if (errors.size <= 1) {
                stringResource(id = descriptionResource) + " ${errors.first().getLabel()}"
            } else {
                val errorTexts = errors.map { it.getLabel() }.joinToString { " - $it \n" }
                stringResource(descriptionResource) + "\n\n" + errorTexts
            }
            Text(text = description)
        }

        BackHandler { onClose() }
    }
}

@Composable
private fun ContactChangeError.getLabel() = stringResource(id = label)
