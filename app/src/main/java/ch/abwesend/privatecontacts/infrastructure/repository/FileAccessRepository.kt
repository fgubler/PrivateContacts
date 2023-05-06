/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import android.content.Context
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
import android.net.Uri
import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.logging.debugLocally
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.importexport.FileContent
import ch.abwesend.privatecontacts.domain.model.result.Result
import ch.abwesend.privatecontacts.domain.repository.FileReadResult
import ch.abwesend.privatecontacts.domain.repository.FileWriteResult
import ch.abwesend.privatecontacts.domain.repository.IFileAccessRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.FileOutputStream

private const val MODE_READ_ONLY = "r"
private const val MODE_WRITE_ONLY = "w"

class FileAccessRepository(private val context: Context) : IFileAccessRepository {
    private val dispatchers: IDispatchers by injectAnywhere()

    override suspend fun readFileContent(fileUri: Uri, requestPermission: Boolean): FileReadResult =
        withContext(dispatchers.io) {
            try {
                logger.debugLocally("Reading content from '${fileUri.path}'")
                val contentResolver = context.contentResolver

                if (requestPermission) {
                    context.grantUriPermission(context.packageName, fileUri, FLAG_GRANT_READ_URI_PERMISSION)
                    contentResolver.takePersistableUriPermission(fileUri, FLAG_GRANT_READ_URI_PERMISSION)
                }

                val content = contentResolver.openFileDescriptor(fileUri, MODE_READ_ONLY)?.use { parcelDescriptor ->
                    FileInputStream(parcelDescriptor.fileDescriptor).use { inputStream ->
                        inputStream.bufferedReader().use { reader ->
                            reader.readText()
                        }
                    }
                }.orEmpty()
                val fileContent = FileContent(content)

                logger.debug("Read ${fileContent.numberOfLines} lines from file")
                Result.Success(value = fileContent)
            } catch (e: Exception) {
                logger.warning("Failed to read file content", e)
                Result.Error(error = e)
            }
        }

    // TODO test manually
    override suspend fun writeFile(file: Uri, fileContent: FileContent, requestPermission: Boolean): FileWriteResult =
        withContext(dispatchers.io) {
            try {
                logger.debugLocally("Writing content to '${file.path}'")
                val contentResolver = context.contentResolver

                if (requestPermission) {
                    context.grantUriPermission(context.packageName, file, FLAG_GRANT_WRITE_URI_PERMISSION)
                    contentResolver.takePersistableUriPermission(file, FLAG_GRANT_WRITE_URI_PERMISSION)
                }

                contentResolver.openFileDescriptor(file, MODE_WRITE_ONLY)?.use { parcelDescriptor ->
                    FileOutputStream(parcelDescriptor.fileDescriptor).use { outputStream ->
                        outputStream.bufferedWriter().use { writer ->
                            writer.write(fileContent.content)
                        }
                    }
                }

                logger.debug("Wrote ${fileContent.numberOfLines} lines to file")
                Result.Success(value = Unit)
            } catch (e: Exception) {
                logger.warning("Failed to write to file", e)
                Result.Error(error = e)
            }
        }
}
