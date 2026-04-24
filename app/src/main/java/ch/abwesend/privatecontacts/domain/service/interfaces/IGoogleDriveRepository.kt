/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service.interfaces

import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveSetupData
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import java.io.File
import com.google.api.services.drive.model.File as DriveFile

/** Abstracts Google Drive file operations given a valid, authenticated session. */
interface IGoogleDriveRepository {
    suspend fun createBackupFolder(): BinaryResult<GoogleDriveSetupData, Exception>

    /** @return true if the folder exists and is accessible for read & write. */
    suspend fun checkFolderAccess(folderId: String, folderName: String): BinaryResult<Boolean, Exception>

    suspend fun findExistingFiles(folderId: String, fileName: String): List<DriveFile>
    suspend fun uploadFile(folderId: String, localFile: File, mimeType: String): DriveFile
}
