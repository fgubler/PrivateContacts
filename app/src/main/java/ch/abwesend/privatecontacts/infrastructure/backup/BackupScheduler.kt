/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.backup

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.service.interfaces.IBackupScheduler
import ch.abwesend.privatecontacts.infrastructure.backup.googledrive.GoogleDriveBackupWorker
import java.util.concurrent.TimeUnit

class BackupScheduler(private val context: Context) : IBackupScheduler {
    private companion object {
        const val WORK_NAME = "periodic_contact_backup_v1"
        const val ONE_TIME_WORK_NAME = "one_time_contact_backup"
        const val DRIVE_WORK_NAME = "periodic_drive_backup_v1"
    }

    override fun schedulePeriodicBackup() {
        schedulePeriodicLocalBackup()
        schedulePeriodicDriveBackup()
    }

    private fun schedulePeriodicLocalBackup() {
        try {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresStorageNotLow(true)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<ContactBackupWorker>(
                repeatInterval = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS,
            )
                .setConstraints(constraints)
                .setInitialDelay(30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                uniqueWorkName = WORK_NAME,
                existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
                request = workRequest,
            )

            logger.info("Periodic backup work scheduled")
        } catch (e: Exception) {
            logger.error("Failed to schedule periodic backup", e)
        }
    }

    override fun triggerOneTimeBackup() {
        try {
            val inputData = workDataOf(ContactBackupWorker.OVERRIDE_BACKUP_FREQUENCY to true)

            val localWork = OneTimeWorkRequestBuilder<ContactBackupWorker>()
                .setInputData(inputData)
                .setInitialDelay(10, TimeUnit.SECONDS)
                .build()

            val driveWork = OneTimeWorkRequestBuilder<GoogleDriveBackupWorker>()
                .setConstraints(getGoogleDriveConstraints())
                .build()

            WorkManager.getInstance(context)
                .beginUniqueWork(ONE_TIME_WORK_NAME, ExistingWorkPolicy.REPLACE, localWork)
                .then(driveWork)
                .enqueue()

            logger.info("One-time backup work triggered (local + Drive) with 10s delay")
        } catch (e: Exception) {
            logger.error("Failed to trigger one-time backup", e)
        }
    }

    private fun schedulePeriodicDriveBackup() {
        try {
            val driveWorkRequest = PeriodicWorkRequestBuilder<GoogleDriveBackupWorker>(
                repeatInterval = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS,
            )
                .setConstraints(getGoogleDriveConstraints())
                .setInitialDelay(1, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                uniqueWorkName = DRIVE_WORK_NAME,
                existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
                request = driveWorkRequest,
            )

            logger.info("Periodic Drive backup work scheduled")
        } catch (e: Exception) {
            logger.error("Failed to schedule periodic Drive backup", e)
        }
    }

    private fun getGoogleDriveConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .setRequiresStorageNotLow(true)
        .setRequiresCharging(false)
        .setRequiresDeviceIdle(false)
        .build()
}
