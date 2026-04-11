/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.backup.googledrive.repository

import android.content.Context
import android.content.Intent
import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveSetupData
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveAuthResult
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveFolderInfo
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.model.result.generic.runCatchingAsResult
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.FileContent
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.withContext
import com.google.api.services.drive.model.File as DriveFile
import java.io.File
import java.util.UUID

// TODO consider for all methods whether the should return BinaryResult objects
// TODO avoid leaking the Drive instance: keep that inside the repository
//  maybe add some initialization-logic and hide the repository behind a construct which only allows further action after initialization or something...
class GoogleDriveRepository(private val context: Context) : IGoogleDriveRepository {
    private val dispatchers: IDispatchers by injectAnywhere()

    companion object {
        private const val BACKUPS_FOLDER_NAME_PREFIX = "PrivateContacts_Backups"
        private const val FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"
        private const val APP_NAME = "PrivateContacts"
    }

    override suspend fun requestAuthorization(): GoogleDriveAuthResult = withContext(dispatchers.io) {
        try {
            val result = authorize()
            if (result.hasResolution()) {
                result.pendingIntent
                    ?.let { GoogleDriveAuthResult.ConsentRequired(it) }
                    ?: GoogleDriveAuthResult.Error
            } else {
                GoogleDriveAuthResult.Authorized(setupFolderFromResult(result))
            }
        } catch (e: Exception) {
            logger.error("Failed to request authorization", e)
            GoogleDriveAuthResult.Error
        }
    }

    override suspend fun handleAuthorizationResult(
        data: Intent?
    ): BinaryResult<GoogleDriveSetupData, Exception> = withContext(dispatchers.io) {
        runCatchingAsResult {
            val result = Identity.getAuthorizationClient(context)
                .getAuthorizationResultFromIntent(data)
            setupFolderFromResult(result)
        }.ifHasError { logger.error("Failed to handle authorization result", it) }
    }

    fun buildDriveService(accessToken: String): Drive {
        val initializer = HttpRequestInitializer { request ->
            request.headers.authorization = "Bearer $accessToken"
        }

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            initializer,
        ).setApplicationName(APP_NAME).build()
    }

    /**
     * Obtains a valid access token silently (i.e. without user interaction).
     * @return the access token string, or null if authorization is not granted.
     */
    suspend fun getAccessTokenSilently(): BinaryResult<String, Unit> = withContext(dispatchers.io) {
        try {
            val result = authorize()
            if (result.hasResolution()) {
                logger.warning("Authorization requires user consent, cannot obtain token silently")
                ErrorResult(Unit)
            } else {
                result.accessToken
                    ?.let { SuccessResult(it) }
                    ?: ErrorResult(Unit)
            }
        } catch (e: Exception) {
            logger.error("Failed to obtain access token", e)
            ErrorResult(Unit)
        }
    }

    fun createBackupFolder(drive: Drive): GoogleDriveFolderInfo {
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

    fun findExistingFiles(drive: Drive, folderId: String, fileName: String): List<DriveFile> {
        val query = "'$folderId' in parents and name = '$fileName' and trashed = false"
        return drive.files().list()
            .setQ(query)
            .setFields("files(id, name)")
            .setSpaces("drive")
            .execute()
            .files
            .orEmpty()
    }

    fun uploadFile(drive: Drive, folderId: String, localFile: File, mimeType: String): DriveFile {
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

    private fun setupFolderFromResult(result: AuthorizationResult): GoogleDriveSetupData {
        val accessToken = result.accessToken
            ?: throw IllegalStateException("Authorization succeeded but no access token returned")

        val drive = buildDriveService(accessToken)
        val folderInfo = createBackupFolder(drive)

        logger.info("Google Drive backup configured with folder: $folderInfo")
        return GoogleDriveSetupData(
            accountEmail = "",
            folderId = folderInfo.id,
            folderName = folderInfo.name,
        )
    }

    private fun buildAuthorizationRequest(): AuthorizationRequest =
        AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(DriveScopes.DRIVE_FILE)))
            .build()

    /**
     * Authorizes via the modern AuthorizationClient API.
     * Must be called on a background thread (uses [Tasks.await]).
     * @return the [AuthorizationResult] containing either an access token or a [android.app.PendingIntent] for consent.
     */
    private suspend fun authorize(): AuthorizationResult = withContext(dispatchers.io) {
        val client = Identity.getAuthorizationClient(context)
        Tasks.await(client.authorize(buildAuthorizationRequest()))
    }
}

