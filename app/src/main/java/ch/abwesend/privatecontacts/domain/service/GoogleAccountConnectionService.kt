/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.service

import android.content.Intent
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.googleaccount.GoogleAccountConnectIntermediateState
import ch.abwesend.privatecontacts.domain.model.googleaccount.GoogleAccountConnectState
import ch.abwesend.privatecontacts.domain.model.googleaccount.toGoogleAccountConnectState
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveAuthResult
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveSetupError
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveAuthorizationRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

/**
 * Drives connecting / switching the single Google account shared by Google Drive backup and contact
 * sync. Unlike the feature-enable flows, this always clears the cached authorization first so the
 * account picker is shown, allowing the user to choose (or switch to) an account.
 *
 * Feature reconciliation after a switch (re-creating the backup folder, re-initializing the sync
 * keycheck) is orchestrated by the caller via the existing [GoogleDriveSetupService] /
 * [SyncSetupService], which authorize silently against the now-connected account.
 */
class GoogleAccountConnectionService {
    private val authRepository: IGoogleDriveAuthorizationRepository by injectAnywhere()

    suspend fun connectAccount(): GoogleAccountConnectIntermediateState {
        authRepository.clearAuthorization()
        return when (val authResult = authRepository.authorize()) {
            is GoogleDriveAuthResult.Authorized -> retrieveAccountEmail(authResult.data)
            is GoogleDriveAuthResult.ConsentRequired -> GoogleAccountConnectState.ConsentRequired(authResult.pendingIntent)
            is GoogleDriveAuthResult.Error -> GoogleDriveSetupError.AUTHORIZATION_FAILED.toGoogleAccountConnectState()
        }
    }

    suspend fun handleConsentResponse(data: Intent?): GoogleAccountConnectIntermediateState =
        when (val authResult = authRepository.authorizeFromIntent(data)) {
            is ErrorResult -> GoogleDriveSetupError.CONSENT_FAILED.toGoogleAccountConnectState()
            is SuccessResult -> retrieveAccountEmail(authResult.value)
        }

    suspend fun disconnectAccount() {
        authRepository.clearAuthorization()
    }

    private suspend fun retrieveAccountEmail(
        repository: IGoogleDriveRepository,
    ): GoogleAccountConnectIntermediateState =
        when (val emailResult = repository.getAccountEmail()) {
            is ErrorResult -> {
                logger.warning("Failed to retrieve account email while connecting", emailResult.error)
                GoogleDriveSetupError.EMAIL_RETRIEVAL_FAILED.toGoogleAccountConnectState()
            }
            is SuccessResult -> GoogleAccountConnectIntermediateState.Success(emailResult.value)
        }
}
