/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.contact

import androidx.annotation.StringRes
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationError
import ch.abwesend.privatecontacts.view.components.dialogs.OkDialog
import ch.abwesend.privatecontacts.view.components.dialogs.SimpleProgressDialog

@Composable
fun DeleteContactsLoadingDialog(deleteMultiple: Boolean) {
    if (deleteMultiple) { // deleting one contact is so fast, a loading-screen does not make sense
        @StringRes val title =
            if (deleteMultiple) R.string.delete_contacts_progress
            else R.string.delete_contact_progress
        SimpleProgressDialog(title = title, allowRunningInBackground = false)
    }
}

@Composable
fun DeleteContactsUnknownErrorDialog(onClose: () -> Unit) {
    OkDialog(title = R.string.error, onClose = onClose) {
        Text(text = stringResource(id = R.string.generic_unknown_error))
    }
}

@Composable
fun DeleteContactsResultDialog(
    numberOfErrors: Int,
    numberOfAttemptedChanges: Int,
    onClose: () -> Unit
) {
    if (numberOfErrors > 0) {
        OkDialog(title = R.string.error, onClose = onClose) {
            @StringRes
            val descriptionResource = when {
                numberOfAttemptedChanges == 1 -> R.string.delete_contact_error
                numberOfAttemptedChanges > numberOfErrors -> R.string.delete_contacts_partial_error
                else -> R.string.delete_contacts_full_error
            }

            Text(text = stringResource(id = descriptionResource))
        }
    }
}

@Composable
fun ChangeContactTypeResultDialog(
    validationErrors: List<ContactValidationError>,
    errors: List<ContactChangeError>,
    numberOfAttemptedChanges: Int,
    onClose: () -> Unit
) {
    if (validationErrors.isNotEmpty() || errors.isNotEmpty()) {
        OkDialog(title = R.string.error, onClose = onClose) {

            val description = if (numberOfAttemptedChanges == 1) {
                @StringRes
                val descriptionResource = when {
                    errors.isNotEmpty() -> errors.first().label
                    validationErrors.isNotEmpty() -> validationErrors.first().label
                    else -> R.string.greetings_from_the_developer // cannot actually happen
                }
                stringResource(id = descriptionResource)
            } else {
                val formatArgs = arrayOf(
                    numberOfAttemptedChanges.toString(),
                    validationErrors.size.toString(),
                    errors.size.toString(),
                )
                stringResource(
                    id = R.string.type_change_batch_error,
                    formatArgs = formatArgs
                )
            }
            Text(text = description)
        }
    }
}
