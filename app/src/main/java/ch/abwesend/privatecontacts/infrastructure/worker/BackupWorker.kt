/*
 * Private Contacts
 * Copyright (c) 2024.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.worker

import android.content.Context
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.backup.BackupFrequency
import ch.abwesend.privatecontacts.domain.model.backup.BackupScope
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.importexport.ContactExportData
import ch.abwesend.privatecontacts.domain.model.importexport.VCardCreateError
import ch.abwesend.privatecontacts.domain.model.importexport.VCardVersion
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.service.ContactExportService
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BackupWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    private val exportService: ContactExportService by injectAnywhere()
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")

    override suspend fun doWork(): Result {
        logger.debug("BackupWorker: Starting backup check")

        try {
            val settings = Settings.current

            // Check if backup is enabled
            if (settings.backupFrequency == BackupFrequency.DISABLED) {
                logger.debug("BackupWorker: Backup is disabled, skipping")
                return Result.success()
            }

            // Check if backup folder is configured
            if (settings.backupFolderUri.isEmpty()) {
                logger.warning("BackupWorker: No backup folder configured, skipping")
                return Result.failure()
            }

            // Check if backup is needed today
            if (!isBackupNeeded(settings.backupFrequency)) {
                logger.debug("BackupWorker: No backup needed today")
                return Result.success()
            }

            // Create backup
            val result = createBackup(settings)

            return when (result) {
                is SuccessResult -> {
                    logger.info("BackupWorker: Backup created successfully")
                    updateLastBackupDate()
                    Result.success()
                }
                is ErrorResult -> {
                    logger.warning("BackupWorker: Backup failed: ${result.error}")
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            logger.error("BackupWorker: Unexpected error during backup", e)
            return Result.failure()
        }
    }

    private fun isBackupNeeded(frequency: BackupFrequency): Boolean {
        val lastBackupDate = getLastBackupDate()
        val today = LocalDate.now()

        return when (frequency) {
            BackupFrequency.DISABLED -> false
            BackupFrequency.DAILY -> lastBackupDate != today
            BackupFrequency.WEEKLY -> {
                val daysSinceLastBackup = java.time.temporal.ChronoUnit.DAYS.between(lastBackupDate, today)
                daysSinceLastBackup >= 7
            }
            BackupFrequency.MONTHLY -> {
                val monthsSinceLastBackup = java.time.temporal.ChronoUnit.MONTHS.between(lastBackupDate, today)
                monthsSinceLastBackup >= 1
            }
        }
    }

    private suspend fun createBackup(settings: ISettingsState): BinaryResult<ContactExportData, VCardCreateError> {
        val timestamp = LocalDateTime.now().format(dateTimeFormatter)
        val scopeText = when (settings.backupScope) {
            BackupScope.ALL -> "all"
            BackupScope.SECRET_ONLY -> "secret"
            BackupScope.PUBLIC_ONLY -> "public"
        }

        val fileName = "backup_${scopeText}_${timestamp}.vcf"
        val backupUri = "${settings.backupFolderUri}/$fileName".toUri()

        // TODO allow VCF version selection
        return when (settings.backupScope) {
            BackupScope.ALL -> exportService.exportAllContacts(backupUri, VCardVersion.V4)
            BackupScope.SECRET_ONLY -> exportService.exportContacts(backupUri, ContactType.SECRET, VCardVersion.V4)
            BackupScope.PUBLIC_ONLY -> exportService.exportContacts(backupUri, ContactType.PUBLIC, VCardVersion.V4)
        }
    }

    private fun getLastBackupDate(): LocalDate {
        // For now, we'll use a simple approach and store the last backup date in SharedPreferences
        // In a more sophisticated implementation, we could store this in the database
        val sharedPrefs = applicationContext.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
        val lastBackupString = sharedPrefs.getString("last_backup_date", null)

        return if (lastBackupString != null) {
            try {
                LocalDate.parse(lastBackupString)
            } catch (e: Exception) {
                LocalDate.MIN // Force backup if parsing fails
            }
        } else {
            LocalDate.MIN // Force backup if no previous date
        }
    }

    private fun updateLastBackupDate() {
        val sharedPrefs = applicationContext.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString("last_backup_date", LocalDate.now().toString())
            .apply()
    }
}
