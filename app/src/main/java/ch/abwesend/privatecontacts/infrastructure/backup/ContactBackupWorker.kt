/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.backup

import android.Manifest.permission.READ_CONTACTS
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.backup.BackupMessage
import ch.abwesend.privatecontacts.domain.model.backup.BackupMessageSeverity
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.importexport.BackupContactScope
import ch.abwesend.privatecontacts.domain.model.importexport.BackupFrequency
import ch.abwesend.privatecontacts.domain.model.importexport.VCardVersion
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.repository.IBackupMessageRepository
import ch.abwesend.privatecontacts.domain.service.ContactExportService
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.ImportExportConstants
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

    private var retryCounter = 0 // the counter will be reset on garbage-collection: should be enough

    companion object {
        const val OVERRIDE_BACKUP_FREQUENCY = "overrideBackupFrequency"
        private const val MAX_RETRY_COUNT = 5
    }

    /** Consider caching them before persisting all at once - however, if we fail that might not work. */
    private suspend fun addErrorMessage(text: String, severity: BackupMessageSeverity) {
        backupMessageRepository.addMessage(BackupMessage(text = text, severity = severity))
    }

    override suspend fun doWork(): Result {
        return try {
            logger.debug("Starting periodic backup")
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
                    text = applicationContext.getString(R.string.backup_folder_not_configured_warning),
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
                    text = applicationContext.getString(R.string.backup_folder_not_writable_error),
                    severity = BackupMessageSeverity.WARNING
                )
                return Result.failure()
            }

            val vCardVersion = VCardVersion.V4 // always use v4 for backups (no loss)
            val dateString = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

            val success = when (settings.backupContactScope) {
                BackupContactScope.ALL -> {
                    coroutineScope {
                        val secretSuccess = async {
                            exportContacts(ContactType.SECRET, dateString, vCardVersion, documentFolder)
                        }
                        val publicSuccess = async {
                            exportContacts(ContactType.PUBLIC, dateString, vCardVersion, documentFolder)
                        }
                        secretSuccess.await() && publicSuccess.await()
                    }
                }
                BackupContactScope.SECRET -> {
                    exportContacts(ContactType.SECRET, dateString, vCardVersion, documentFolder)
                }
                BackupContactScope.PUBLIC -> {
                    exportContacts(ContactType.PUBLIC, dateString, vCardVersion, documentFolder)
                }
            }

            Settings.repository.lastBackupDate = LocalDate.now()

            if (success) {
                logger.debug("Periodic backup completed successfully")
                Result.success()
            } else {
                logger.warning("Periodic backup completed with failures")
                addErrorMessage(
                    text = applicationContext.getString(R.string.backup_completed_with_failures_warning),
                    severity = BackupMessageSeverity.ERROR
                )
                Result.failure()
            }
        } catch (e: CancellationException) {
            logger.debug("Periodic backup cancelled", e)
            retryCounter++
            if (retryCounter > MAX_RETRY_COUNT) {
                logger.error("Periodic backup failed due to cancellation", e)
                retryCounter = 0
                Result.failure()
            } else {
                logger.warning("Periodic backup cancelled in attempt $retryCounter: re-trying")
                Result.retry()
            }
        } catch (e: Exception) {
            logger.error("Periodic backup failed", e)
            Result.failure()
        }
    }

    private suspend fun exportContacts(
        type: ContactType,
        dateString: String,
        vCardVersion: VCardVersion,
        documentFolder: DocumentFile,
    ): Boolean {
        if (type == ContactType.PUBLIC && !hasAndroidContactsPermission()) {
            logger.warning("Skipping backup of public contacts: READ_CONTACTS permission not granted")
            addErrorMessage(
                text = applicationContext.getString(R.string.backup_permission_missing_warning),
                severity = BackupMessageSeverity.WARNING
            )
            return false
        }

        val fileName = when (type) {
            ContactType.SECRET -> "backup_secret_$dateString.vcf"
            ContactType.PUBLIC -> "backup_public_$dateString.vcf"
        }

        return exportToBackupFile(
            folder = documentFolder,
            fileName = fileName,
            contactType = type,
            vCardVersion = vCardVersion
        )
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
    ): Boolean {
        val existingFile = folder.findFile(fileName)
        existingFile?.delete()

        val file = folder.createFile(ImportExportConstants.VCF_MAIN_MIME_TYPE, fileName)
        if (file == null) {
            logger.warning("Failed to create backup file: $fileName")
            addErrorMessage(
                text = applicationContext.getString(R.string.backup_file_creation_failed_error),
                severity = BackupMessageSeverity.ERROR
            )
            return false
        }

        val result = exportService.exportContacts(
            targetFile = file.uri,
            sourceType = contactType,
            vCardVersion = vCardVersion,
            requestPermission = false, // we already have permission for the folder
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

    private fun isBackupDue(frequency: BackupFrequency, lastBackupDate: LocalDate): Boolean {
        val today = LocalDate.now()

        return when (frequency) {
            BackupFrequency.DISABLED -> false
            BackupFrequency.DAILY -> ChronoUnit.DAYS.between(lastBackupDate, today) >= 1
            BackupFrequency.WEEKLY -> ChronoUnit.DAYS.between(lastBackupDate, today) >= 7
            BackupFrequency.MONTHLY -> ChronoUnit.MONTHS.between(lastBackupDate, today) >= 1
        }
    }
}
