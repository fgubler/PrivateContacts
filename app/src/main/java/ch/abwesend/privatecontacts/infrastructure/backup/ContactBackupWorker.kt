/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.backup

import android.Manifest.permission.READ_CONTACTS
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.backup.BackupContactScope
import ch.abwesend.privatecontacts.domain.model.backup.BackupFrequency
import ch.abwesend.privatecontacts.domain.model.backup.BackupMessage
import ch.abwesend.privatecontacts.domain.model.backup.BackupMessageSeverity
import ch.abwesend.privatecontacts.domain.model.backup.NumberOfBackupsToKeep
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.importexport.VCardVersion
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.repository.IBackupMessageRepository
import ch.abwesend.privatecontacts.domain.repository.IEncryptionRepository
import ch.abwesend.privatecontacts.domain.repository.IFileAccessRepository
import ch.abwesend.privatecontacts.domain.service.ContactExportService
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.ImportExportConstants.CRYPT_FILE_EXTENSION
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.ImportExportConstants.CRYPT_PRETENDING_MIME_TYPE
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.ImportExportConstants.VCF_FILE_EXTENSION
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.ImportExportConstants.VCF_MAIN_MIME_TYPE
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class ContactBackupWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    private val exportService: ContactExportService by injectAnywhere()
    private val backupMessageRepository: IBackupMessageRepository by injectAnywhere()
    private val fileAccessRepository: IFileAccessRepository by injectAnywhere()
    private val encryptionRepository: IEncryptionRepository by injectAnywhere()
    private val backupNotificationRepository: BackupNotificationRepository by injectAnywhere()

    companion object {
        const val OVERRIDE_BACKUP_FREQUENCY = "overrideBackupFrequency"
        const val MAX_RETRY_COUNT = 20
        private var retryCounter = 0 // the counter will be reset on garbage-collection
    }

    private suspend fun addErrorMessage(@StringRes textRes: Int, severity: BackupMessageSeverity) {
        val text = applicationContext.getString(textRes)
        addErrorMessage(text = text, severity = severity)
    }

    /** Caching them would not work because they would be lost during a crash. */
    private suspend fun addErrorMessage(text: String, severity: BackupMessageSeverity) {
        backupMessageRepository.addMessage(BackupMessage(text = text, severity = severity))
    }

    override suspend fun doWork(): Result {
        return try {
            logger.debug("Starting periodic backup")
            backupMessageRepository.clearMessages() // a new start with a clean slate
            val settings = Settings.nextOrDefault()
            val overrideFrequency = inputData.getBoolean(OVERRIDE_BACKUP_FREQUENCY, defaultValue = false)

            if (settings.backupFrequency == BackupFrequency.DISABLED) {
                logger.debug("Periodic backup is disabled, skipping")
                return Result.success()
            }

            val backupFolder = settings.backupFolder
            if (backupFolder.isEmpty()) {
                logger.warning("Backup folder not configured, skipping backup")
                addErrorMessage(
                    textRes = R.string.backup_folder_not_configured_warning,
                    severity = BackupMessageSeverity.WARNING
                )
                return Result.failure()
            }

            if (!overrideFrequency && !isBackupDue(settings.backupFrequency, settings.lastBackupDate)) {
                logger.debug("Backup not yet due, skipping")
                return Result.success()
            }

            val folderUri = backupFolder.toUri()
            val documentFolder = DocumentFile.fromTreeUri(applicationContext, folderUri)
            if (documentFolder == null || !documentFolder.canWrite()) {
                logger.warning("Cannot write to backup folder: $backupFolder")
                addErrorMessage(
                    textRes = R.string.backup_folder_not_writable_error,
                    severity = BackupMessageSeverity.WARNING
                )
                return Result.failure()
            }

            val vCardVersion = VCardVersion.V4 // always use v4 for backups (no loss)
            val dateString = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val encryptionPassword = resolveEncryptionPassword(settings)

            val success = when (settings.backupContactScope) {
                BackupContactScope.ALL -> {
                    coroutineScope {
                        val secretSuccess = async {
                            exportContacts(ContactType.SECRET, dateString, vCardVersion, documentFolder, encryptionPassword)
                        }
                        val publicSuccess = async {
                            exportContacts(ContactType.PUBLIC, dateString, vCardVersion, documentFolder, encryptionPassword)
                        }
                        secretSuccess.await() && publicSuccess.await()
                    }
                }
                BackupContactScope.SECRET -> {
                    exportContacts(ContactType.SECRET, dateString, vCardVersion, documentFolder, encryptionPassword)
                }
                BackupContactScope.PUBLIC -> {
                    exportContacts(ContactType.PUBLIC, dateString, vCardVersion, documentFolder, encryptionPassword)
                }
            }

            Settings.repository.lastBackupDate = LocalDate.now()
            retryCounter = 0

            if (success) {
                logger.debug("Periodic backup completed successfully")
                cleanupOldBackups(settings.numberOfBackupsToKeep, documentFolder)
                Result.success()
            } else {
                logger.warning("Periodic backup completed with failures")
                addErrorMessage(
                    textRes = R.string.backup_completed_with_failures_warning,
                    severity = BackupMessageSeverity.ERROR
                )
                Result.failure()
            }
        } catch (e: CancellationException) {
            logger.debug("Periodic backup cancelled", e)
            retryCounter++

            // randomness to avoid an infinite loop if JVM resets retryCounter
            if (retryCounter < MAX_RETRY_COUNT && Math.random() > 0.01) {
                logger.warning("Periodic backup cancelled in attempt $retryCounter: re-trying")
                Result.retry()
            } else {
                logger.error("Periodic backup failed due to cancellation in attempt $retryCounter", e)
                retryCounter = 0
                Result.failure()
            }
        } catch (e: Exception) {
            retryCounter = 0
            logger.error("Periodic backup failed", e)
            Result.failure()
        }
    }

    private suspend fun resolveEncryptionPassword(settings: ISettingsState): String? {
        return if (!settings.backupEncryptionEnabled || settings.backupPasswordEncrypted.isEmpty()) {
            null
        } else {
            when (val result = encryptionRepository.decryptPassword(settings.backupPasswordEncrypted)) {
                is SuccessResult -> result.value
                is ErrorResult -> {
                    logger.warning("Failed to decrypt backup password; disabling encryption", result.error)
                    Settings.repository.backupEncryptionEnabled = false
                    addErrorMessage(
                        textRes = R.string.backup_encryption_password_recovery_failed_error,
                        severity = BackupMessageSeverity.ERROR
                    )
                    null
                }
            }
        }
    }

    private suspend fun exportContacts(
        type: ContactType,
        dateString: String,
        vCardVersion: VCardVersion,
        documentFolder: DocumentFile,
        encryptionPassword: String?,
    ): Boolean {
        if (type == ContactType.PUBLIC && !hasAndroidContactsPermission()) {
            logger.warning("Skipping backup of public contacts: READ_CONTACTS permission not granted")
            addErrorMessage(
                textRes = R.string.backup_permission_missing_warning,
                severity = BackupMessageSeverity.WARNING
            )
            return false
        }

        val extension = if (encryptionPassword == null) VCF_FILE_EXTENSION else CRYPT_FILE_EXTENSION
        val fileNamePrefix = getFilenamePrefix(type)
        val fileName = "$fileNamePrefix$dateString.$extension"
        cleanupExistingFile(documentFolder, fileName)

        return exportToBackupFile(
            folder = documentFolder,
            fileName = fileName,
            contactType = type,
            vCardVersion = vCardVersion,
            encryptionPassword = encryptionPassword,
        )
    }

    private fun cleanupExistingFile(documentFolder: DocumentFile, fileName: String) {
        try {
            val existingFile = documentFolder.findFile(fileName)
            existingFile?.let { fileAccessRepository.deleteFileIfEmpty(it) }
        } catch (e: Exception) {
            logger.warning("Failed to potentially delete empty pre-existing backup file", e)
        }
    }

    private fun hasAndroidContactsPermission(): Boolean {
        val response = ContextCompat.checkSelfPermission(applicationContext, READ_CONTACTS)
        return response == PackageManager.PERMISSION_GRANTED
    }

    private suspend fun exportToBackupFile(
        folder: DocumentFile,
        fileName: String,
        contactType: ContactType,
        vCardVersion: VCardVersion,
        encryptionPassword: String?,
    ): Boolean {
        val mimeType = if (encryptionPassword == null) VCF_MAIN_MIME_TYPE else CRYPT_PRETENDING_MIME_TYPE
        val file = folder.createFile(mimeType, fileName)
        if (file == null) {
            logger.warning("Failed to create backup file: $fileName")
            addErrorMessage(
                textRes = R.string.backup_file_creation_failed_error,
                severity = BackupMessageSeverity.ERROR
            )
            return false
        }

        val result = exportService.exportContacts(
            targetFile = file.uri,
            sourceType = contactType,
            vCardVersion = vCardVersion,
            requestPermission = false, // we already have permission for the folder
            encryptionPassword = encryptionPassword,
        )

        return when (result) {
            is SuccessResult -> true
            is ErrorResult -> {
                logger.warning("Failed to export $contactType contacts for backup: ${result.error}")
                val contactTypeLabel = applicationContext.getString(contactType.label)
                addErrorMessage(
                    text = applicationContext.getString(R.string.backup_export_failed_error, contactTypeLabel),
                    severity = BackupMessageSeverity.ERROR
                )
                false
            }
        }
    }

    private suspend fun cleanupOldBackups(numberOfBackupsToKeep: NumberOfBackupsToKeep, documentFolder: DocumentFile) {
        ContactType.entries.forEach { type ->
            try {
                val prefix = getFilenamePrefix(type)
                val backupFiles = documentFolder.listFiles()
                    .filterNotNull()
                    .filter { it.name?.startsWith(prefix) == true }
                    .sortedBy { it.name } // that also sorts by date (ascending)

                if (backupFiles.size > 2) {
                    val secondNewestFile = backupFiles[backupFiles.lastIndex - 1] // not the one we just created
                    val deleted = fileAccessRepository.deleteFileIfEmpty(secondNewestFile)
                    if (deleted) {
                        logger.debug("Deleted second newest backup file: ${secondNewestFile.name}")
                        // if it was empty, this cleanup did not run => the one before might be empty, too.
                        cleanupOldBackups(numberOfBackupsToKeep, documentFolder)
                        return
                    }
                }

                val toDelete = (backupFiles.size - numberOfBackupsToKeep.maxCount).coerceAtLeast(0)
                backupFiles.take(toDelete).forEach { file ->
                    logger.debug("Deleting old backup: ${file.name}")
                    file.delete()
                }
                logger.info("Deleted $toDelete old backups for $type")
            } catch (e: Exception) {
                logger.warning("Failed to delete old backups for $type", e)
                addErrorMessage(
                    textRes = R.string.backup_delete_old_failed_warning,
                    severity = BackupMessageSeverity.WARNING
                )
            }
        }
    }

    private fun isBackupDue(frequency: BackupFrequency, lastBackupDate: LocalDate): Boolean {
        val today = LocalDate.now()

        return when (frequency) {
            BackupFrequency.DISABLED -> false
            BackupFrequency.DAILY -> ChronoUnit.DAYS.between(lastBackupDate, today) >= 1
            BackupFrequency.WEEKLY -> ChronoUnit.DAYS.between(lastBackupDate, today) >= 7
            BackupFrequency.MONTHLY -> ChronoUnit.MONTHS.between(lastBackupDate, today) >= 1
        }
    }

    private fun getFilenamePrefix(type: ContactType): String = when (type) {
        ContactType.SECRET -> "backup_secret_"
        ContactType.PUBLIC -> "backup_public_"
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        logger.info("Creating foreground info for periodic backup")
        return backupNotificationRepository.createForegroundInfo()
    }
}
