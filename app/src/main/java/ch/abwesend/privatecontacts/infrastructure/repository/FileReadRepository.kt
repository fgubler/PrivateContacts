/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import android.content.Context
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.net.Uri
import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.logging.debugLocally
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.importexport.FileContent
import ch.abwesend.privatecontacts.domain.model.result.Result
import ch.abwesend.privatecontacts.domain.repository.FileReadResult
import ch.abwesend.privatecontacts.domain.repository.IFileReadRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.withContext
import java.io.FileInputStream

private const val MODE_READ_ONLY = "r"

class FileReadRepository(private val context: Context) : IFileReadRepository {
    private val dispatchers: IDispatchers by injectAnywhere()

    override suspend fun readFileContent(fileUri: Uri, requestPermission: Boolean): FileReadResult =
        withContext(dispatchers.io) {
            try {
                logger.debugLocally("Reading lines from '${fileUri.path}'")
                val contentResolver = context.contentResolver

                if (requestPermission) {
                    context.grantUriPermission(context.packageName, fileUri, FLAG_GRANT_READ_URI_PERMISSION)
                    contentResolver.takePersistableUriPermission(fileUri, FLAG_GRANT_READ_URI_PERMISSION)
                }

                val lines = contentResolver.openFileDescriptor(fileUri, MODE_READ_ONLY)?.use { parcelDescriptor ->
                    FileInputStream(parcelDescriptor.fileDescriptor).use { fileDescriptor ->
                        fileDescriptor.bufferedReader().use { reader ->
                            reader.readLines()
                        }
                    }
                }.orEmpty()

                logger.debug("Read ${lines.size} lines from file")
                Result.Success(value = FileContent(lines))
            } catch (e: Exception) {
                logger.warning("Failed to read file content", e)
                Result.Error(error = e)
            }
        }
}
