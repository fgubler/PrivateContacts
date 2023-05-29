/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.dialogs

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.abwesend.privatecontacts.R

@Composable
fun GenericUnknownErrorDialog(onClose: () -> Unit) {
    OkDialog(title = R.string.error, onClose = onClose) {
        Text(text = stringResource(id = R.string.generic_unknown_error))
    }
}
