package ch.abwesend.privatecontacts.domain.service

import android.content.Intent
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveAuthResult
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveIntermediateSetupState
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveSetupError
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveSetupState
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.toDriveSetupState
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveAuthorizationRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveRepository
import ch.abwesend.privatecontacts.domain.settings.ISettingsState
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

class GoogleDriveSetupService {
    private val authRepository: IGoogleDriveAuthorizationRepository by injectAnywhere()
    private val settingsState: ISettingsState by injectAnywhere()

    suspend fun requestGoogleDriveAuthorization(): GoogleDriveIntermediateSetupState {
        authRepository.clearAuthorization()
        val result = authRepository.authorize()
        return handleGoogleDriveAuthorizationResult(result)
    }

    suspend fun handleGoogleDriveConsentResponse(data: Intent?): GoogleDriveIntermediateSetupState {
        return when (val authResult = authRepository.authorizeFromIntent(data)) {
            is ErrorResult -> GoogleDriveSetupError.CONSENT_FAILED.toDriveSetupState()
            is SuccessResult -> onGoogleDriveAuthorized(authResult.value)
        }
    }

    private suspend fun handleGoogleDriveAuthorizationResult(
        result: GoogleDriveAuthResult<IGoogleDriveRepository>,
    ): GoogleDriveIntermediateSetupState {
        return when (result) {
            is GoogleDriveAuthResult.Authorized -> onGoogleDriveAuthorized(result.data)
            is GoogleDriveAuthResult.ConsentRequired -> GoogleDriveSetupState.ConsentRequired(result.pendingIntent)
            is GoogleDriveAuthResult.Error -> GoogleDriveSetupError.AUTHORIZATION_FAILED.toDriveSetupState()
        }
    }

    private suspend fun onGoogleDriveAuthorized(repository: IGoogleDriveRepository): GoogleDriveIntermediateSetupState {
        return when (val emailResult = repository.getAccountEmail()) {
            is ErrorResult -> {
                logger.warning("Failed to retrieve Google Drive account email", emailResult.error)
                GoogleDriveSetupError.EMAIL_RETRIEVAL_FAILED.toDriveSetupState()
            }
            is SuccessResult -> {
                logger.info("Retrieved Google Drive account email: ${emailResult.value}")
                createDriveBackupFolder(repository, emailResult.value)
            }
        }
    }

    private suspend fun createDriveBackupFolder(
        repository: IGoogleDriveRepository,
        accountEmail: String,
    ): GoogleDriveIntermediateSetupState {
        when (val canReuseFolder = canReuseBackupFolder(repository)) {
            is ErrorResult -> return canReuseFolder.error.toDriveSetupState()
            is SuccessResult -> {
                if (canReuseFolder.value) {
                    return GoogleDriveIntermediateSetupState.Success(
                        accountEmail = accountEmail,
                        backupEnabled = true,
                        folderId = settingsState.googleDriveFolderId,
                        folderName = settingsState.googleDriveFolderName,
                    )
                }
            }
        }

        return when (val setupData = repository.createBackupFolder()) {
            is SuccessResult -> {
                logger.info("Created Google Drive folder: ${setupData.value.folderName} (${setupData.value.folderId})")
                GoogleDriveIntermediateSetupState.Success(
                    accountEmail = accountEmail,
                    backupEnabled = true,
                    folderId = setupData.value.folderId,
                    folderName = setupData.value.folderName,
                )
            }
            is ErrorResult -> GoogleDriveSetupError.FOLDER_CREATION_FAILED.toDriveSetupState()
        }
    }

    private suspend fun canReuseBackupFolder(
        repository: IGoogleDriveRepository
    ): BinaryResult<Boolean, GoogleDriveSetupError> {
        val existingFolderId = settingsState.googleDriveFolderId
        val existingFolderName = settingsState.googleDriveFolderName

        if (existingFolderId.isEmpty() || existingFolderName.isEmpty()) {
            return SuccessResult(false)
        }

        return repository.hasFolderAccess(existingFolderId, existingFolderName)
            .ifHasValue { hasAccess ->
                if (hasAccess) {
                    logger.info("Reusing existing Google Drive folder: $existingFolderName ($existingFolderId)")
                } else {
                    logger.info("Stored Google Drive folder no longer accessible; creating a new one.")
                }
            }.mapError {
                logger.warning("Failed to check folder access", it)
                GoogleDriveSetupError.FOLDER_ACCESS_CHECK_FAILED
            }
    }
}
