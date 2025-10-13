/*
 * Private Contacts
 * Copyright (c) 2024.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.worker

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.backup.BackupFrequency
import ch.abwesend.privatecontacts.domain.model.importexport.VCardVersion
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.service.ContactExportService
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val exportService: ContactExportService by injectAnywhere()

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

    private suspend fun createBackup(settings: ch.abwesend.privatecontacts.domain.settings.ISettingsState): ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult<ch.abwesend.privatecontacts.domain.model.importexport.ContactExportData, ch.abwesend.privatecontacts.domain.model.importexport.VCardCreateError> {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
        val scopeText = when (settings.backupScope) {
            ch.abwesend.privatecontacts.domain.model.backup.BackupScope.ALL -> "all"
            ch.abwesend.privatecontacts.domain.model.backup.BackupScope.SECRET_ONLY -> "secret"
            ch.abwesend.privatecontacts.domain.model.backup.BackupScope.PUBLIC_ONLY -> "public"
        }

        val fileName = "backup_${scopeText}_${timestamp}.vcf"
        val backupUri = Uri.parse("${settings.backupFolderUri}/$fileName")

        val contactType = settings.backupScope.toContactType()

        return if (contactType != null) {
            // Backup specific contact type
            exportService.exportContacts(backupUri, contactType, VCardVersion.V4)
        } else {
            // Backup all contacts - we need to implement this differently
            // For now, let's backup secret contacts as the default
            exportService.exportContacts(backupUri, ch.abwesend.privatecontacts.domain.model.contact.ContactType.SECRET, VCardVersion.V4)
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
