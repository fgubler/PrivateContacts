/*
 * Private Contacts
 * Copyright (c) 2025.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.components.dialogs

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import ch.abwesend.privatecontacts.R

@Composable
fun SaveCancelDialog(
    @StringRes title: Int,
    content: @Composable () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) = YesNoDialog(
    title = title,
    text = content,
    yesButtonEnabled = true,
    yesButtonLabel = R.string.save,
    noButtonLabel = R.string.cancel,
    onYes = onSave,
    onNo = onCancel
)