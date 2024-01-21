/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.util

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import ch.abwesend.privatecontacts.R

@ExperimentalFoundationApi
fun Modifier.longClickForCopyToClipboard(
    textToCopy: String,
    onClick: () -> Unit = { },
): Modifier = composed {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    combinedClickable(
        onClick = onClick,
        onLongClick = {
            val text = AnnotatedString(text = textToCopy)
            clipboardManager.setText(text)
            Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
        },
    )
}
