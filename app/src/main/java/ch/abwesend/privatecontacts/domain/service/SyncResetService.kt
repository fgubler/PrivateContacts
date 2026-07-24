/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveAuthResult
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveAuthorizationRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

/**
 * Wipes the remote sync state: deletes every file in the Drive app-data folder (keycheck, protocol,
 * command/batch logs and snapshots) so synchronization can be started over from scratch. This is
 * intentionally destructive - there is no recovery of the previously synchronized history.
 */
class SyncResetService {
    private val authRepository: IGoogleDriveAuthorizationRepository by injectAnywhere()

    /**
     * Best-effort deletion of all remote sync data. Requires an already-granted app-data
     * authorization (which an enabled sync always has); a pending consent is treated as an error
     * because a reset must not trigger an interactive account/consent round-trip.
     *
     * @return the number of deleted files on success.
     */
    suspend fun deleteAllRemoteSyncData(): BinaryResult<Int, Exception> =
        when (val authResult = authRepository.authorize()) {
            is GoogleDriveAuthResult.Authorized -> deleteAllFiles(authResult.data)
            is GoogleDriveAuthResult.ConsentRequired ->
                ErrorResult(IllegalStateException("Reset requires an existing authorization"))
            is GoogleDriveAuthResult.Error ->
                ErrorResult(IllegalStateException("Failed to authorize Google Drive for reset"))
        }

    private suspend fun deleteAllFiles(repository: IGoogleDriveRepository): BinaryResult<Int, Exception> {
        val files = repository.listAppDataFiles()
        var deletedCount = 0
        for (file in files) {
            if (repository.deleteFile(file.id)) {
                deletedCount++
            } else {
                logger.warning("Failed to delete remote sync file ${file.name} during reset")
            }
        }
        logger.info("Sync reset deleted $deletedCount of ${files.size} remote files")
        return SuccessResult(deletedCount)
    }
}
