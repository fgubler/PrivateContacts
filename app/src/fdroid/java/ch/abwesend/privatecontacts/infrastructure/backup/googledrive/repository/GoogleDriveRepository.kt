/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.backup.googledrive.repository

import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveFile
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveFolder
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveRepository
import java.io.File

class GoogleDriveRepository : IGoogleDriveRepository {
    private val unsupported get() = UnsupportedOperationException("Google Drive is not available in the F-Droid build")

    override suspend fun getAccountEmail(): BinaryResult<String, Exception> = ErrorResult(unsupported)

    override suspend fun createBackupFolder(): BinaryResult<GoogleDriveFolder, Exception> = ErrorResult(unsupported)

    override suspend fun hasFolderAccess(folderId: String, folderName: String): BinaryResult<Boolean, Exception> =
        ErrorResult(unsupported)

    override suspend fun findExistingFiles(folderId: String, fileName: String): List<GoogleDriveFile> = emptyList()
    override suspend fun listAllFiles(folderId: String): List<GoogleDriveFile> = emptyList()
    override suspend fun deleteFile(fileId: String): Boolean = false

    override suspend fun uploadFile(folderId: String, localFile: File, mimeType: String): GoogleDriveFile? = null
}
