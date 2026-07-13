/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import android.content.Intent
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveAuthResult
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.model.sync.SyncIntermediateSetupState
import ch.abwesend.privatecontacts.domain.model.sync.SyncSetupError
import ch.abwesend.privatecontacts.domain.model.sync.SyncSetupState
import ch.abwesend.privatecontacts.domain.model.sync.toSyncSetupState
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveAuthorizationRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.ISyncKeyCheckService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

/**
 * Drives the "enable sync" flow: authorize Google Drive (adding the appdata scope may require a
 * one-time consent), then verify the passphrase against `keycheck.json` (or initialize it on the
 * first device) before sync is switched on.
 */
class SyncSetupService {
    private val authRepository: IGoogleDriveAuthorizationRepository by injectAnywhere()
    private val keyCheckService: ISyncKeyCheckService by injectAnywhere()

    suspend fun enableSync(passphrase: String): SyncIntermediateSetupState =
        when (val result = authRepository.authorize()) {
            is GoogleDriveAuthResult.Authorized -> onAuthorized(result.data, passphrase)
            is GoogleDriveAuthResult.ConsentRequired -> SyncSetupState.ConsentRequired(result.pendingIntent)
            is GoogleDriveAuthResult.Error -> SyncSetupError.AUTHORIZATION_FAILED.toSyncSetupState()
        }

    suspend fun handleConsentResponse(data: Intent?, passphrase: String): SyncIntermediateSetupState =
        when (val authResult = authRepository.authorizeFromIntent(data)) {
            is ErrorResult -> SyncSetupError.CONSENT_FAILED.toSyncSetupState()
            is SuccessResult -> onAuthorized(authResult.value, passphrase)
        }

    private suspend fun onAuthorized(
        repository: IGoogleDriveRepository,
        passphrase: String,
    ): SyncIntermediateSetupState =
        when (val emailResult = repository.getAccountEmail()) {
            is ErrorResult -> {
                logger.warning("Failed to retrieve account email for sync", emailResult.error)
                SyncSetupError.EMAIL_RETRIEVAL_FAILED.toSyncSetupState()
            }
            is SuccessResult -> verifyPassphrase(repository, passphrase, emailResult.value)
        }

    private suspend fun verifyPassphrase(
        repository: IGoogleDriveRepository,
        passphrase: String,
        accountEmail: String,
    ): SyncIntermediateSetupState =
        when (keyCheckService.verifyOrInitialize(repository, passphrase)) {
            ISyncKeyCheckService.Outcome.MATCH,
            ISyncKeyCheckService.Outcome.INITIALIZED -> SyncIntermediateSetupState.Success(accountEmail)
            ISyncKeyCheckService.Outcome.MISMATCH -> SyncSetupError.WRONG_PASSPHRASE.toSyncSetupState()
            ISyncKeyCheckService.Outcome.ERROR -> SyncSetupError.UNKNOWN.toSyncSetupState()
        }
}
