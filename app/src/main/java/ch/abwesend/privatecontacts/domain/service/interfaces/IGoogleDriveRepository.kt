/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service.interfaces

import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveFolder
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import java.io.File
import com.google.api.services.drive.model.File as DriveFile

/** Abstracts Google Drive file operations given a valid, authenticated session. */
interface IGoogleDriveRepository {
    suspend fun getAccountEmail(): BinaryResult<String, Exception>

    suspend fun createBackupFolder(): BinaryResult<GoogleDriveFolder, Exception>

    /** @return true if the folder exists and is accessible for read & write. */
    suspend fun hasFolderAccess(folderId: String, folderName: String): BinaryResult<Boolean, Exception>

    suspend fun findExistingFiles(folderId: String, fileName: String): List<DriveFile>
    suspend fun uploadFile(folderId: String, localFile: File, mimeType: String): DriveFile
}
