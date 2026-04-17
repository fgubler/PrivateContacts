/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.backup.googledrive.repository

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveFolderInfo
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveSetupData
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.generic.runCatchingAsResult
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveRepository
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import java.io.File
import java.util.UUID
import com.google.api.services.drive.model.File as DriveFile

class GoogleDriveRepository(private val drive: Drive) : IGoogleDriveRepository {
    companion object {
        private const val BACKUPS_FOLDER_NAME_PREFIX = "PrivateContacts_Backups"
        private const val FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"
    }

    override fun createBackupFolder(): BinaryResult<GoogleDriveSetupData, Exception> = runCatchingAsResult {
        val folderInfo = createFolder()
        logger.info("Google Drive backup configured with folder: $folderInfo")

        GoogleDriveSetupData(
            accountEmail = "",
            folderId = folderInfo.id,
            folderName = folderInfo.name,
        )
    }

    override fun findExistingFiles(folderId: String, fileName: String): List<DriveFile> {
        val query = "'$folderId' in parents and name = '$fileName' and trashed = false"
        return drive.files().list()
            .setQ(query)
            .setFields("files(id, name)")
            .setSpaces("drive")
            .execute()
            .files
            .orEmpty()
    }

    override fun uploadFile(folderId: String, localFile: File, mimeType: String): DriveFile {
        val metadata = DriveFile().apply {
            name = localFile.name
            parents = listOf(folderId)
        }
        val content = FileContent(mimeType, localFile)
        val uploaded = drive.files().create(metadata, content)
            .setFields("id, name")
            .execute()
        logger.info("Uploaded file to Google Drive: ${uploaded.name} (${uploaded.id})")
        return uploaded
    }

    private fun createFolder(): GoogleDriveFolderInfo {
        val folderName = "${BACKUPS_FOLDER_NAME_PREFIX}_${UUID.randomUUID()}"
        val metadata = DriveFile().apply {
            name = folderName
            mimeType = FOLDER_MIME_TYPE
        }
        val folder = drive.files().create(metadata)
            .setFields("id, name")
            .execute()
        logger.info("Created Google Drive folder: ${folder.name} (${folder.id})")
        return GoogleDriveFolderInfo(id = folder.id, name = folder.name)
    }
}
