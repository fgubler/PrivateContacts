/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import android.os.Environment
import androidx.annotation.VisibleForTesting
import ch.abwesend.privatecontacts.domain.lib.logging.debugLocally
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import java.io.File

@VisibleForTesting
internal const val FILE_PICKER_URI_PATH_PREFIX = "/document/raw:"

open class FilePickerSanitizingService {
    /**
     * @return the valid, existing [File] or null if the path is invalid or the file does not exist
     */
    fun getValidFileOrNull(filePath: String): File? = runCatching {
        val sanitizedPath = sanitizeFilePath(filePath)
        val file = File(sanitizedPath)

        logger.debugLocally("Selected file '${ file.absolutePath ?: "NO_FILE" }'")
        file.takeIf { fileExists(it) }
    }.getOrElse {
        logger.warning("Failed to sanitize file path", it)
        null
    }

    @VisibleForTesting
    protected fun sanitizeFilePath(filePath: String): String {
        val rootDirectory = Environment.getExternalStorageDirectory().absolutePath
        return if (filePath.startsWith(rootDirectory, ignoreCase = true)) filePath
        else if (filePath.contains(rootDirectory, ignoreCase = true)) {
            val startingIndex = filePath.indexOf(rootDirectory, ignoreCase = true)
            filePath.substring(startingIndex)
        } else if (filePath.contains(FILE_PICKER_URI_PATH_PREFIX)) {
            logger.warning("File path does not contain root directory: might be invalid")
            filePath.replace(oldValue = FILE_PICKER_URI_PATH_PREFIX, newValue = "").trim()
        } else {
            logger.warning("File path is probably invalid: cannot sanitize")
            filePath
        }
    }

    @VisibleForTesting
    protected open fun fileExists(file: File): Boolean = file.exists()
}
