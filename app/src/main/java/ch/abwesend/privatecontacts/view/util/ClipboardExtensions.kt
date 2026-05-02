/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.util

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import ch.abwesend.privatecontacts.R
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
fun Modifier.longClickForCopyToClipboard(
    textToCopy: String,
    onClick: () -> Unit = { },
): Modifier = composed {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    combinedClickable(
        onClick = onClick,
        onLongClick = {
            coroutineScope.launch {
                clipboard.setClipEntry(ClipData.newPlainText(textToCopy, textToCopy).toClipEntry())
            }
            Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
        },
    )
}
