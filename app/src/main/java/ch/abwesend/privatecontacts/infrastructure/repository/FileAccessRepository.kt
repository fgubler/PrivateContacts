/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import android.content.ContentResolver
import android.content.Context
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
import android.net.Uri
import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.logging.debugLocally
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.importexport.FileContent
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
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
                    requestReadPermission(contentResolver, fileUri)
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
                SuccessResult(value = fileContent)
            } catch (e: Exception) {
                logger.warning("Failed to read file content", e)
                ErrorResult(error = e)
            }
        }

    override suspend fun writeFile(fileContent: FileContent, file: Uri, requestPermission: Boolean): FileWriteResult =
        withContext(dispatchers.io) {
            try {
                logger.debugLocally("Writing content to '${file.path}'")
                val contentResolver = context.contentResolver

                if (requestPermission) {
                    requestWritePermission(contentResolver, file)
                }

                contentResolver.openFileDescriptor(file, MODE_WRITE_ONLY)?.use { parcelDescriptor ->
                    FileOutputStream(parcelDescriptor.fileDescriptor).use { outputStream ->
                        outputStream.bufferedWriter().use { writer ->
                            writer.write(fileContent.content)
                        }
                    }
                }

                logger.debug("Wrote ${fileContent.numberOfLines} lines to file")
                SuccessResult(value = Unit)
            } catch (e: Exception) {
                logger.warning("Failed to write to file", e)
                ErrorResult(error = e)
            }
        }

    private fun requestReadPermission(contentResolver: ContentResolver, fileUri: Uri) {
        requestPermission(contentResolver, fileUri, FLAG_GRANT_READ_URI_PERMISSION)
    }

    private fun requestWritePermission(contentResolver: ContentResolver, fileUri: Uri) {
        requestPermission(contentResolver, fileUri, FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    private fun requestPermission(contentResolver: ContentResolver, fileUri: Uri, permissionFlag: Int) {
        context.grantUriPermission(context.packageName, fileUri, permissionFlag)
        try {
            contentResolver.takePersistableUriPermission(fileUri, permissionFlag)
        } catch (e: SecurityException) {
            logger.debugLocally("Failed to get persistable permission for file: ${fileUri.path}", e)
            // the exception-text might contain user-information (the path): do not log it to crashlytics
            logger.warning("Failed to get persistable permission for file: $permissionFlag")
        }
    }
}
