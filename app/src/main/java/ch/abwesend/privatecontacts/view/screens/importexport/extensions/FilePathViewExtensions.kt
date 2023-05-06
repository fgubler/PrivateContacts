/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.screens.importexport.extensions

import android.net.Uri
import android.os.Environment
import androidx.annotation.VisibleForTesting
import ch.abwesend.privatecontacts.domain.lib.logging.logger

@VisibleForTesting
internal const val FILE_PICKER_URI_PATH_PREFIX = "/document/raw:"

fun Uri?.getFilePathForDisplay(): String {
    val separator = '/'
    val filePath = this?.path ?: return ""

    val rootDirectory = Environment.getExternalStorageDirectory().absolutePath
        .orEmpty()
        .trim()
        .trimEnd(separator)
        .let { "$it$separator" }

    val path = when {
        filePath.contains(rootDirectory, ignoreCase = true) -> {
            val startingIndex = filePath.indexOf(rootDirectory, ignoreCase = true)
            val fullPath = filePath.substring(startingIndex)
            val lengthToDrop = (rootDirectory.length - 1).coerceAtLeast(minimumValue = 0)

            if (fullPath.length > rootDirectory.length) fullPath
                .drop(lengthToDrop)
                .trimStart(separator)
            else fullPath
        }
        filePath.contains(FILE_PICKER_URI_PATH_PREFIX) -> {
            logger.info("File path does not contain root directory: unexpected")
            filePath.replace(oldValue = FILE_PICKER_URI_PATH_PREFIX, newValue = "")
        }
        else -> {
            logger.info("File path does not follow expected pattern")
            filePath
        }
    }

    return path.trim()
}
