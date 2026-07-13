/*
 * Private Contacts
 * Copyright (c) 2022.
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
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.service.interfaces.ISyncScheduler
import ch.abwesend.privatecontacts.infrastructure.backup.worker.SyncPullWorker
import ch.abwesend.privatecontacts.infrastructure.backup.worker.SyncUploadWorker
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

class SyncScheduler(private val context: Context) : ISyncScheduler {
    private companion object {
        const val UPLOAD_WORK_NAME = "sync_upload_v1"
        const val PERIODIC_PULL_WORK_NAME = "periodic_sync_pull_v1"
        const val ONE_TIME_PULL_WORK_NAME = "one_time_sync_pull"

        const val UPLOAD_DEBOUNCE_SECONDS = 30L
        const val PERIODIC_PULL_HOURS = 6L
    }

    override fun scheduleUploadDebounced() {
        try {
            val workRequest = OneTimeWorkRequestBuilder<SyncUploadWorker>()
                .setConstraints(getConstraints())
                .setInitialDelay(UPLOAD_DEBOUNCE_SECONDS, TimeUnit.SECONDS)
                .build()

            // APPEND_OR_REPLACE (not KEEP): a save that lands after the worker started reading the
            // outbox must still schedule a follow-up run rather than be silently dropped (issue 4).
            WorkManager.getInstance(context).enqueueUniqueWork(
                uniqueWorkName = UPLOAD_WORK_NAME,
                existingWorkPolicy = ExistingWorkPolicy.APPEND_OR_REPLACE,
                request = workRequest,
            )
            logger.debug("Debounced sync upload scheduled")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error("Failed to schedule sync upload", e)
        }
    }

    override fun schedulePeriodicPull() {
        try {
            val workRequest = PeriodicWorkRequestBuilder<SyncPullWorker>(
                repeatInterval = PERIODIC_PULL_HOURS,
                repeatIntervalTimeUnit = TimeUnit.HOURS,
            )
                .setConstraints(getConstraints())
                .setInitialDelay(1, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                uniqueWorkName = PERIODIC_PULL_WORK_NAME,
                existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
                request = workRequest,
            )
            logger.info("Periodic sync pull scheduled")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error("Failed to schedule periodic sync pull", e)
        }
    }

    override fun triggerPullNow() {
        try {
            val workRequest = OneTimeWorkRequestBuilder<SyncPullWorker>()
                .setConstraints(getConstraints())
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                uniqueWorkName = ONE_TIME_PULL_WORK_NAME,
                existingWorkPolicy = ExistingWorkPolicy.REPLACE,
                request = workRequest,
            )
            logger.debug("One-time sync pull triggered")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error("Failed to trigger sync pull", e)
        }
    }

    private fun getConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .setRequiresStorageNotLow(true)
        .setRequiresCharging(false)
        .setRequiresDeviceIdle(false)
        .build()
}
