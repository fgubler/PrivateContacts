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
import ch.abwesend.privatecontacts.domain.model.importexport.BinaryFileContent
import ch.abwesend.privatecontacts.domain.model.importexport.TextFileContent
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.repository.BinaryFileReadResult
import ch.abwesend.privatecontacts.domain.repository.FileReadResult
import ch.abwesend.privatecontacts.domain.repository.FileWriteResult
import ch.abwesend.privatecontacts.domain.repository.IFileAccessRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.FileOutputStream

private const val MODE_READ_ONLY = "r"
private const val MODE_WRITE_ONLY = "w"
private const val MAX_FILE_SIZE_IN_BYTES = 1024 * 1024 * 10 // 10 MB

class FileAccessRepository(private val context: Context) : IFileAccessRepository {
    private val dispatchers: IDispatchers by injectAnywhere()

    override suspend fun readTextFileContent(fileUri: Uri, requestPermission: Boolean): FileReadResult =
        try {
            val content = readFileContent(fileUri, requestPermission) { inputStream ->
                inputStream.bufferedReader().use { reader ->
                    reader.readText()
                }
            }.orEmpty()
            val fileContent = TextFileContent(content)

            logger.debug("Read ${fileContent.numberOfLines} lines from file")
            SuccessResult(value = fileContent)
        } catch (e: Exception) {
            logger.warning("Failed to read file content", e)
            ErrorResult(error = e)
        }

    override suspend fun readBinaryFileContent(fileUri: Uri, requestPermission: Boolean): BinaryFileReadResult =
        try {
            val content = readFileContent(fileUri, requestPermission) { inputStream ->
                inputStream.readBytes()
            } ?: ByteArray(0)

            if (content.size > MAX_FILE_SIZE_IN_BYTES) {
                throw IllegalArgumentException("File is too large: ${content.size / 1024 / 1024}MB!")
            }

            val fileContent = BinaryFileContent(content)
            logger.debug("Read binary file")
            SuccessResult(value = fileContent)
        } catch (e: Exception) {
            logger.warning("Failed to read file content", e)
            ErrorResult(error = e)
        }

    private suspend fun <T> readFileContent(fileUri: Uri, requestPermission: Boolean, readContent: suspend (FileInputStream) -> T): T? =
        withContext(dispatchers.io) {
            logger.debugLocally("Reading content from '${fileUri.path}'")
            val contentResolver = context.contentResolver

            if (requestPermission) {
                requestReadPermission(contentResolver, fileUri)
            }

            contentResolver.openFileDescriptor(fileUri, MODE_READ_ONLY)?.use { parcelDescriptor ->
                FileInputStream(parcelDescriptor.fileDescriptor).use { inputStream ->
                    readContent(inputStream)
                }
            }
        }

    override suspend fun writeFile(fileContent: TextFileContent, file: Uri, requestPermission: Boolean): FileWriteResult =
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
