/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.backup.googledrive.repository

import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveFile
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveFolder
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveFolderInfo
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.generic.runCatchingAsResult
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import com.google.api.services.drive.model.File as DriveFile

class GoogleDriveRepository(private val drive: Drive) : IGoogleDriveRepository {
    private val dispatchers: IDispatchers by injectAnywhere()

    companion object {
        private const val BACKUPS_FOLDER_NAME_PREFIX = "PrivateContacts_Backups"
        private const val FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"
    }

    override suspend fun getAccountEmail(): BinaryResult<String, Exception> =
        withContext(dispatchers.io) {
            runCatchingAsResult {
                drive.about().get()
                    .setFields("user")
                    .execute()
                    .user
                    .emailAddress
            }
        }

    override suspend fun createBackupFolder(): BinaryResult<GoogleDriveFolder, Exception> =
        withContext(dispatchers.io) {
            runCatchingAsResult {
                val folderInfo = createFolder()
                logger.info("Google Drive backup configured with folder: $folderInfo")

                GoogleDriveFolder(
                    folderId = folderInfo.id,
                    folderName = folderInfo.name,
                )
            }
        }

    override suspend fun hasFolderAccess(folderId: String, folderName: String): BinaryResult<Boolean, Exception> =
        withContext(dispatchers.io) {
            runCatchingAsResult {
                drive.files().get(folderId)
                    .setFields("id, name, trashed, capabilities")
                    .execute()
                    ?.let { file ->
                        val nameMatches = file.name == folderName
                        val notTrashed = file.trashed != true
                        val canWrite = file.capabilities?.canEdit == true
                        nameMatches && notTrashed && canWrite
                    } ?: false
            }
        }

    override suspend fun findExistingFiles(folderId: String, fileName: String): List<GoogleDriveFile> =
        withContext(dispatchers.io) {
            val query = "'$folderId' in parents and name = '$fileName' and trashed = false"
            drive.files().list()
                .setQ(query)
                .setFields("files(id, name)")
                .setSpaces("drive")
                .execute()
                .files
                .orEmpty()
                .mapNotNull { file ->
                    file.id?.let { id -> file.name?.let { name -> GoogleDriveFile(id, name) } }
                }
        }

    override suspend fun uploadFile(folderId: String, localFile: File, mimeType: String): GoogleDriveFile? =
        withContext(dispatchers.io) {
            val metadata = DriveFile().apply {
                name = localFile.name
                parents = listOf(folderId)
            }
            val content = FileContent(mimeType, localFile)
            val uploaded = drive.files().create(metadata, content)
                .setFields("id, name")
                .execute()
            logger.info("Uploaded file to Google Drive: ${uploaded.name} (${uploaded.id})")
            uploaded.id?.let { id -> uploaded.name?.let { name -> GoogleDriveFile(id, name) } }
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
