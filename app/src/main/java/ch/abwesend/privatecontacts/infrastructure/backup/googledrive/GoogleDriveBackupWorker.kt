/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.backup.googledrive

import android.content.Context
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.coroutine.mapAsync
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.backup.BackupMessage
import ch.abwesend.privatecontacts.domain.model.backup.BackupMessageSeverity
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveAuthResult
import ch.abwesend.privatecontacts.domain.model.importexport.resolveContactTypes
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.repository.IBackupMessageRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveAuthenticationRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveRepository
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.backup.BackupConstants.PUBLIC_BACKUP_PREFIX
import ch.abwesend.privatecontacts.infrastructure.backup.BackupConstants.SECRET_BACKUP_PREFIX
import ch.abwesend.privatecontacts.infrastructure.backup.BackupNotificationRepository
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.ImportExportConstants.CRYPT_FILE_EXTENSION
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.ImportExportConstants.CRYPT_PRETENDING_MIME_TYPE
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.ImportExportConstants.VCF_FILE_EXTENSION
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.ImportExportConstants.VCF_MAIN_MIME_TYPE
import java.io.File
import java.util.UUID

class GoogleDriveBackupWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    private val googleDriveAuthRepository: IGoogleDriveAuthenticationRepository by injectAnywhere()
    private val backupMessageRepository: IBackupMessageRepository by injectAnywhere()
    private val backupNotificationRepository: BackupNotificationRepository by injectAnywhere()

    private suspend fun addErrorMessage(text: String, severity: BackupMessageSeverity) {
        backupMessageRepository.addDriveMessage(BackupMessage(text = text, severity = severity))
    }

    override suspend fun doWork(): Result {
        return try {
            logger.debug("Starting Google Drive backup upload")
            val settings = Settings.nextOrDefault()

            val metaData = when (val result = checkPreConditions(settings)) {
                is SuccessResult -> result.value
                is ErrorResult -> return result.error
            }

            val driveRepository = when (val result = getGoogleDriveRepository()) {
                is SuccessResult -> result.value
                is ErrorResult -> return result.error
            }

            val contactTypes = settings.backupContactScope.resolveContactTypes()
            val encrypted = settings.backupEncryptionEnabled

            val retryRequired = contactTypes
                .mapAsync { type -> uploadLocalBackup(driveRepository, metaData, type, encrypted) }
                .any { it == UploadResult.RETRY }

            if (retryRequired) {
                logger.warning("Google Drive backup upload completed with failures")
                Result.retry()
            } else {
                logger.debug("Google Drive backup upload completed successfully")
                Result.success()
            }
        } catch (e: Exception) {
            logger.error("Google Drive backup upload failed", e)
            addErrorMessage(
                text = applicationContext.getString(R.string.drive_backup_upload_failed_error),
                severity = BackupMessageSeverity.ERROR,
            )
            Result.retry()
        }
    }

    /**
     * @return [SuccessResult] if all preconditions are met and the backup should continue.
     *  [ErrorResult] if any preconditions are not met and the backup should be aborted.
     */
    private suspend fun checkPreConditions(settings: ISettingsState): BinaryResult<MetaData, Result> {
        if (!settings.googleDriveBackupEnabled) {
            logger.debug("Google Drive backup is disabled, skipping")
            return ErrorResult(Result.success())
        }

        val folderId = settings.googleDriveFolderId
        if (folderId.isEmpty()) {
            logger.warning("Google Drive folder not configured, skipping")
            addErrorMessage(
                text = applicationContext.getString(R.string.drive_backup_folder_not_configured_warning),
                severity = BackupMessageSeverity.WARNING,
            )
            return ErrorResult(Result.failure())
        }

        val backupFolder = settings.backupFolder
        if (backupFolder.isEmpty()) {
            logger.warning("Local backup folder not configured, skipping Drive upload")
            addErrorMessage(
                text = applicationContext.getString(R.string.drive_backup_local_folder_not_configured_warning),
                severity = BackupMessageSeverity.WARNING,
            )
            return ErrorResult(Result.success())
        }

        val documentFolder = DocumentFile.fromTreeUri(applicationContext, backupFolder.toUri())
        if (documentFolder == null || !documentFolder.canRead()) {
            logger.warning("Cannot read local backup folder")
            addErrorMessage(
                text = applicationContext.getString(R.string.drive_backup_local_folder_unreadable_error),
                severity = BackupMessageSeverity.ERROR,
            )
            return ErrorResult(Result.retry())
        }

        return SuccessResult(MetaData(folderId = folderId, localBackupFolder = documentFolder))
    }

    private suspend fun getGoogleDriveRepository(): BinaryResult<IGoogleDriveRepository, Result> {
        val unauthorizedWarning = "No Google authorization available, skipping Drive backup"

        return when (val driveResult = googleDriveAuthRepository.authorize()) {
            is GoogleDriveAuthResult.Authorized -> SuccessResult(driveResult.data)
            is GoogleDriveAuthResult.ConsentRequired -> {
                logger.warning("Authorization requires user consent, cannot obtain token silently")
                logger.warning(unauthorizedWarning)
                addErrorMessage(
                    text = applicationContext.getString(R.string.drive_backup_account_not_signed_in_error),
                    severity = BackupMessageSeverity.ERROR,
                )
                ErrorResult(Result.failure())
            }
            is GoogleDriveAuthResult.Error -> {
                logger.warning(unauthorizedWarning)
                addErrorMessage(
                    text = applicationContext.getString(R.string.drive_backup_account_not_signed_in_error),
                    severity = BackupMessageSeverity.ERROR,
                )
                ErrorResult(Result.failure())
            }
        }
    }

    private suspend fun uploadLocalBackup(
        driveRepository: IGoogleDriveRepository,
        metaData: MetaData,
        type: ContactType,
        encrypted: Boolean,
    ): UploadResult {
        val newestFile = findNewestLocalBackup(metaData.localBackupFolder, type, encrypted)
        if (newestFile == null) {
            logger.warning("No local backup found for $type contacts")
            addErrorMessage(
                text = applicationContext.getString(R.string.drive_backup_no_local_file_warning, type.name.lowercase()),
                severity = BackupMessageSeverity.WARNING,
            )
            return UploadResult.ABORT
        }

        val alreadyUploaded = driveRepository
            .findExistingFiles(metaData.folderId, newestFile.name.orEmpty())
            .isNotEmpty()

        if (alreadyUploaded) {
            logger.debug("Backup ${newestFile.name} already uploaded to Drive")
            return UploadResult.SUCCESS
        }

        val success = uploadLocalBackup(
            driveRepository = driveRepository,
            folderId = metaData.folderId,
            documentFile = newestFile,
            encrypted = encrypted
        )
        return if (success) UploadResult.SUCCESS else UploadResult.RETRY
    }

    private fun findNewestLocalBackup(
        folder: DocumentFile,
        type: ContactType,
        encrypted: Boolean,
    ): DocumentFile? {
        val prefix = when (type) {
            ContactType.SECRET -> SECRET_BACKUP_PREFIX
            ContactType.PUBLIC -> PUBLIC_BACKUP_PREFIX
        }
        val extension = if (encrypted) CRYPT_FILE_EXTENSION else VCF_FILE_EXTENSION

        return folder.listFiles()
            .filter { file ->
                val name = file.name
                !name.isNullOrEmpty() && name.startsWith(prefix) && name.endsWith(".$extension")
            }
            .maxByOrNull { it.name.orEmpty() } // date-based naming ensures lexicographic = chronological
    }

    private suspend fun uploadLocalBackup(
        driveRepository: IGoogleDriveRepository,
        folderId: String,
        documentFile: DocumentFile,
        encrypted: Boolean,
    ): Boolean {
        return try {
            val tempFile = copyToTempFile(documentFile) ?: return false
            try {
                val mimeType = if (encrypted) CRYPT_PRETENDING_MIME_TYPE else VCF_MAIN_MIME_TYPE
                driveRepository.uploadFile(folderId, tempFile, mimeType)
                true
            } finally {
                tempFile.delete()
            }
        } catch (e: Exception) {
            logger.error("Failed to upload ${documentFile.name} to Google Drive", e)
            addErrorMessage(
                text = applicationContext.getString(
                    R.string.drive_backup_upload_file_failed_error,
                    documentFile.name.orEmpty(),
                ),
                severity = BackupMessageSeverity.ERROR,
            )
            false
        }
    }

    private fun copyToTempFile(documentFile: DocumentFile): File? {
        val name = documentFile.name ?: UUID.randomUUID().toString()
        val tempFile = File(applicationContext.cacheDir, name)
        return applicationContext.contentResolver.openInputStream(documentFile.uri)
            ?.use { input -> tempFile.outputStream().use { output -> input.copyTo(output) } }
            ?.let { tempFile }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return backupNotificationRepository.createForegroundInfo()
    }
}

private data class MetaData(
    val folderId: String,
    val localBackupFolder: DocumentFile,
)

private enum class UploadResult {
    SUCCESS,
    ABORT,
    RETRY,
}