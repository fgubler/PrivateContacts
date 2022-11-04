/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.contactmenu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError
import ch.abwesend.privatecontacts.domain.model.result.ContactValidationError
import ch.abwesend.privatecontacts.view.components.dialogs.OkDialog

@Composable
fun ChangeContactsUnknownErrorDialog(onClose: () -> Unit) {
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
        val description = when {
            numberOfAttemptedChanges == 1 -> stringResource(id = R.string.delete_contact_error)
            numberOfAttemptedChanges > numberOfErrors -> {
                val formatArgs = arrayOf(numberOfErrors, numberOfAttemptedChanges)
                stringResource(id = R.string.delete_contacts_partial_error, formatArgs = formatArgs)
            }
            else -> stringResource(id = R.string.delete_contacts_full_error)
        }

        OkDialog(title = R.string.error, onClose = onClose) {
            Text(text = description)
        }
    }
}

@Composable
fun ChangeContactTypeErrorDialog(
    validationErrors: List<ContactValidationError>,
    errors: List<ContactChangeError>,
    numberOfAttemptedChanges: Int,
    numberOfSuccessfulChanges: Int,
    onClose: () -> Unit
) {
    if (validationErrors.isNotEmpty() || errors.isNotEmpty()) {
        OkDialog(title = R.string.error, onClose = onClose) {

            val mainText = when {
                numberOfAttemptedChanges == 1 -> stringResource(id = R.string.type_change_error)
                numberOfSuccessfulChanges > 0 -> {
                    val numberOfFailedChanges = numberOfAttemptedChanges - numberOfSuccessfulChanges
                    val formatArgs = arrayOf(
                        numberOfAttemptedChanges.toString(),
                        numberOfFailedChanges.toString(),
                        validationErrors.size.toString(),
                        errors.size.toString(),
                    )
                    stringResource(
                        id = R.string.type_change_batch_error,
                        formatArgs = formatArgs
                    )
                }
                else -> stringResource(id = R.string.make_contacts_secret_full_error)
            }

            val errorDescriptions = errors.distinct().map { stringResource(id = it.label) }
            val errorsTitle = stringResource(id = R.string.errors)

            val validationErrorDescriptions = validationErrors.distinct().map { stringResource(id = it.label) }
            val validationErrorsTitle = stringResource(id = R.string.validation_errors)

            val scrollState = rememberScrollState()
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                Text(text = mainText)
                ErrorsChapter(title = errorsTitle, errorTexts = errorDescriptions)
                ErrorsChapter(title = validationErrorsTitle, errorTexts = validationErrorDescriptions)
            }
        }
    }
}

@Composable
private fun ErrorsChapter(title: String, errorTexts: List<String>) {
    if (errorTexts.isNotEmpty()) {
        Spacer(modifier = Modifier.height(15.dp))
        Text(text = title, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(5.dp))
        errorTexts.forEach {
            Spacer(modifier = Modifier.height(5.dp))
            Text(text = it, fontStyle = FontStyle.Italic)
        }
    }
}
