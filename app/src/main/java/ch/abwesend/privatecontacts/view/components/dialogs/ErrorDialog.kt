/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.dialogs

import androidx.annotation.StringRes
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import ch.abwesend.privatecontacts.R

@Composable
fun ErrorDialog(
    errorMessage: String,
    @StringRes okButtonLabel: Int = R.string.close,
    onClose: () -> Unit,
) = OkDialog(title = R.string.error, okButtonLabel = okButtonLabel, onClose = onClose) {
    Text(text = errorMessage)
}
