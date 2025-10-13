/*
 * Private Contacts
 * Copyright (c) 2024.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.backup.BackupFrequency
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.infrastructure.worker.BackupWorker
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class BackupSchedulerService(private val context: Context) {
    
    private val workManager = WorkManager.getInstance(context)
    
    companion object {
        private const val BACKUP_WORK_NAME = "periodic_backup_work"
        private val BACKUP_TIME = LocalTime.of(1, 0) // 1:00 AM
    }

    /**
     * Schedules or reschedules the periodic backup based on current settings
     */
    fun scheduleBackup() {
        val settings = Settings.current
        
        when (settings.backupFrequency) {
            BackupFrequency.DISABLED -> {
                cancelBackup()
            }
            BackupFrequency.DAILY,
            BackupFrequency.WEEKLY,
            BackupFrequency.MONTHLY -> {
                schedulePeriodicBackup()
            }
        }
    }

    /**
     * Cancels the periodic backup
     */
    fun cancelBackup() {
        logger.debug("BackupScheduler: Cancelling periodic backup")
        workManager.cancelUniqueWork(BACKUP_WORK_NAME)
    }

    private fun schedulePeriodicBackup() {
        logger.debug("BackupScheduler: Scheduling periodic backup")
        
        // Calculate initial delay to start at 1am tomorrow (or today if it's before 1am)
        val initialDelay = calculateInitialDelay()
        
        // Create constraints - backup should run when device is charging and has storage space
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // No network needed for local backup
            .setRequiresCharging(false) // Don't require charging to avoid missing backups
            .setRequiresDeviceIdle(false) // Don't require idle to ensure backup happens
            .setRequiresBatteryNotLow(true) // Only when battery is not low
            .setRequiresStorageNotLow(true) // Only when storage is not low
            .build()

        // Create the periodic work request - runs daily at 1am
        // The worker itself will determine if a backup is actually needed based on frequency
        val backupWorkRequest = PeriodicWorkRequestBuilder<BackupWorker>(
            repeatInterval = 1, // Check daily
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MINUTES)
            .build()

        // Schedule the work, replacing any existing work
        workManager.enqueueUniquePeriodicWork(
            BACKUP_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            backupWorkRequest
        )
        
        logger.info("BackupScheduler: Periodic backup scheduled with initial delay of $initialDelay minutes")
    }

    private fun calculateInitialDelay(): Long {
        val now = LocalDateTime.now()
        val targetTime = LocalTime.of(1, 0) // 1:00 AM
        
        // Calculate next occurrence of 1:00 AM
        val nextBackupTime = if (now.toLocalTime().isBefore(targetTime)) {
            // If it's before 1 AM today, schedule for today at 1 AM
            now.toLocalDate().atTime(targetTime)
        } else {
            // If it's after 1 AM today, schedule for tomorrow at 1 AM
            now.toLocalDate().plusDays(1).atTime(targetTime)
        }
        
        return ChronoUnit.MINUTES.between(now, nextBackupTime)
    }

    /**
     * Triggers an immediate backup (for testing or manual backup)
     */
    fun triggerImmediateBackup() {
        logger.debug("BackupScheduler: Triggering immediate backup")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .setRequiresStorageNotLow(true)
            .build()

        val immediateBackupRequest = androidx.work.OneTimeWorkRequestBuilder<BackupWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueue(immediateBackupRequest)
    }
}