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
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.service.interfaces.IBackupScheduler
import java.util.concurrent.TimeUnit

class BackupScheduler(private val context: Context) : IBackupScheduler {
    private companion object {
        const val WORK_NAME = "periodic_contact_backup_v1"
        const val ONE_TIME_WORK_NAME = "one_time_contact_backup"
    }

    override fun schedulePeriodicBackup() {
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

            val workRequest = OneTimeWorkRequestBuilder<ContactBackupWorker>()
                .setInputData(inputData)
                .setInitialDelay(10, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )

            logger.info("One-time backup work triggered with 10s delay")
        } catch (e: Exception) {
            logger.error("Failed to trigger one-time backup", e)
        }
    }
}
