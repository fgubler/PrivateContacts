/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.backup.util

import androidx.work.ListenableWorker
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import kotlinx.coroutines.CancellationException

class WorkerErrorHandler {
    companion object {
        const val MAX_RETRY_COUNT = 20
    }

    private var retryCounter = 0 // the counter will be reset on garbage-collection

    suspend fun doWorkWithErrorHandling(
        workDescription: String,
        addPersistedErrorMessage: suspend (Int, Array<out String>) -> Unit,
        block: suspend () -> ListenableWorker.Result
    ): ListenableWorker.Result = try {
        block().also {
            retryCounter = 0 // reset the counter after a run without exception
        }
    } catch (e: CancellationException) {
        logger.debug("$workDescription cancelled", e)
        retryCounter++

        // randomness to avoid an infinite loop if JVM resets retryCounter
        if (retryCounter < MAX_RETRY_COUNT && Math.random() > 0.01) {
            logger.warning("$workDescription cancelled in attempt $retryCounter: re-trying")
            ListenableWorker.Result.retry()
        } else {
            logger.error("$workDescription failed due to cancellation in attempt $retryCounter", e)
            retryCounter = 0
            ListenableWorker.Result.failure()
        }
    } catch (e: Exception) {
        retryCounter = 0
        logger.error("$workDescription failed", e)
        addPersistedErrorMessage(R.string.backup_worker_failed_unexpectedly_error, arrayOf(workDescription))
        ListenableWorker.Result.failure()
    }
}
