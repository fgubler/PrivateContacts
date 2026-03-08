/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.backup

import android.content.Context
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.importexport.BackupContactScope
import ch.abwesend.privatecontacts.domain.model.importexport.BackupFrequency
import ch.abwesend.privatecontacts.domain.model.importexport.VCardVersion
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.service.ContactExportService
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.view.screens.importexport.extensions.ImportExportConstants
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class ContactBackupWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    private val exportService: ContactExportService by injectAnywhere()

    override suspend fun doWork(): Result {
        return try {
            logger.debug("Starting periodic backup")
            val settings = Settings.nextOrDefault()

            if (settings.backupFrequency == BackupFrequency.DISABLED) {
                logger.debug("Periodic backup is disabled, skipping")
                return Result.success()
            }

            val backupFolder = settings.backupFolder
            if (backupFolder.isEmpty()) {
                logger.warning("Backup folder not configured, skipping backup")
                return Result.success()
            }

            if (!isBackupDue(settings.backupFrequency, settings.lastBackupDate)) {
                logger.debug("Backup not yet due, skipping")
                return Result.success()
            }

            val folderUri = backupFolder.toUri()
            val documentFolder = DocumentFile.fromTreeUri(applicationContext, folderUri)
            if (documentFolder == null || !documentFolder.canWrite()) {
                logger.warning("Cannot write to backup folder: $backupFolder")
                return Result.failure()
            }

            val vCardVersion = settings.defaultVCardVersion
            val dateString = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

            when (settings.backupContactScope) {
                BackupContactScope.ALL -> {
                    exportToBackupFile(documentFolder, "backup_secret_$dateString.vcf", ContactType.SECRET, vCardVersion)
                    exportToBackupFile(documentFolder, "backup_public_$dateString.vcf", ContactType.PUBLIC, vCardVersion)
                }
                BackupContactScope.SECRET -> {
                    exportToBackupFile(documentFolder, "backup_secret_$dateString.vcf", ContactType.SECRET, vCardVersion)
                }
                BackupContactScope.PUBLIC -> {
                    exportToBackupFile(documentFolder, "backup_public_$dateString.vcf", ContactType.PUBLIC, vCardVersion)
                }
            }

            Settings.repository.lastBackupDate = LocalDate.now()

            logger.debug("Periodic backup completed successfully")
            Result.success()
        } catch (e: Exception) {
            logger.error("Periodic backup failed", e)
            Result.failure()
        }
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
            return false
        }

        val result = exportService.exportContacts(
            targetFile = file.uri,
            sourceType = contactType,
            vCardVersion = vCardVersion
        )

        return when (result) {
            is SuccessResult -> true
            is ErrorResult -> {
                logger.warning("Failed to export $contactType contacts for backup: ${result.error}")
                false
            }
        }
    }

    private fun isBackupDue(frequency: BackupFrequency, lastBackupDate: LocalDate?): Boolean {
        if (lastBackupDate == null) return true

        val today = LocalDate.now()

        return when (frequency) {
            BackupFrequency.DISABLED -> false
            BackupFrequency.DAILY -> ChronoUnit.DAYS.between(lastBackupDate, today) >= 1
            BackupFrequency.WEEKLY -> ChronoUnit.DAYS.between(lastBackupDate, today) >= 7
            BackupFrequency.MONTHLY -> ChronoUnit.MONTHS.between(lastBackupDate, today) >= 1
        }
    }
}
