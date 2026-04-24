/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveSetupState
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveAuthResult
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveSetupError
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.toDriveSetupState
import ch.abwesend.privatecontacts.domain.model.result.generic.BinaryResult
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.repository.IEncryptionRepository
import ch.abwesend.privatecontacts.domain.service.DatabaseService
import ch.abwesend.privatecontacts.domain.service.LauncherAppearanceService
import ch.abwesend.privatecontacts.domain.service.interfaces.IBackupScheduler
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveAuthenticationRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.PermissionService
import ch.abwesend.privatecontacts.domain.settings.SettingsRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    private val databaseService: DatabaseService by injectAnywhere()
    private val launcherAppearanceService: LauncherAppearanceService by injectAnywhere()
    private val permissionService: PermissionService by injectAnywhere()
    private val backupScheduler: IBackupScheduler by injectAnywhere()
    private val encryptionRepository: IEncryptionRepository by injectAnywhere()
    private val settingsRepository: SettingsRepository by injectAnywhere()
    private val googleDriveAuthRepository: IGoogleDriveAuthenticationRepository by injectAnywhere()

    private val _driveSetupState = MutableStateFlow<GoogleDriveSetupState>(GoogleDriveSetupState.Inactive)
    val driveSetupState: StateFlow<GoogleDriveSetupState> = _driveSetupState.asStateFlow()

    fun initialize(settingsRepository: SettingsRepository) {
        if (!permissionService.hasContactReadPermission()) {
            settingsRepository.blockIncomingCallsFromUnknownNumbers = false
            settingsRepository.showAndroidContacts = false
            settingsRepository.defaultContactType = ContactType.default
        }
    }

    suspend fun resetDatabase(): Boolean {
        val result = viewModelScope.async { databaseService.resetDatabase() }
        delay(2000) // let the user wait a bit
        return result.await()
    }

    fun changeLauncherAppearance(hideAppPurpose: Boolean): Boolean {
        try {
            if (hideAppPurpose) {
                launcherAppearanceService.useCalculatorAppearance()
            } else {
                launcherAppearanceService.useDefaultAppearance()
            }
            return true
        } catch (e: Exception) {
            logger.error("Failed to change launcher appearance", e)
            return false
        }
    }

    fun triggerOneTimeBackup() {
        backupScheduler.triggerOneTimeBackup()
    }

    fun encryptBackupPassword(password: String) {
        when (val result = encryptionRepository.encryptPassword(password)) {
            is SuccessResult -> {
                settingsRepository.backupPasswordEncrypted = result.value
                settingsRepository.backupEncryptionEnabled = true
            }
            is ErrorResult -> logger.warning("Failed to encrypt backup password", result.error)
        }
    }

    fun disableBackupEncryption() {
        settingsRepository.backupEncryptionEnabled = false
        settingsRepository.backupPasswordEncrypted = ""
        encryptionRepository.deleteKeyStoreKey()
    }

    fun requestGoogleDriveAuthorization() {
        _driveSetupState.withLoadingState {
            val result = googleDriveAuthRepository.authorize()
            handleGoogleDriveAuthorizationResult(result)
        }
    }

    private suspend fun handleGoogleDriveAuthorizationResult(
        result: GoogleDriveAuthResult<IGoogleDriveRepository>,
    ): GoogleDriveSetupState {
        return when (result) {
            is GoogleDriveAuthResult.Authorized -> onGoogleDriveAuthorized(result.data)
            is GoogleDriveAuthResult.ConsentRequired -> GoogleDriveSetupState.ConsentRequired(result.pendingIntent)
            is GoogleDriveAuthResult.Error -> GoogleDriveSetupError.AUTHORIZATION_FAILED.toDriveSetupState()
        }
    }

    fun handleGoogleDriveConsentResponse(data: Intent?) {
        _driveSetupState.withLoadingState {
            when (val authResult = googleDriveAuthRepository.authorizeFromIntent(data)) {
                is ErrorResult -> GoogleDriveSetupError.CONSENT_FAILED.toDriveSetupState()
                is SuccessResult -> onGoogleDriveAuthorized(authResult.value)
            }
        }
    }

    private suspend fun onGoogleDriveAuthorized(repository: IGoogleDriveRepository): GoogleDriveSetupState {
        return when (val emailResult = repository.getAccountEmail()) {
            is ErrorResult -> {
                logger.warning("Failed to retrieve Google Drive account email", emailResult.error)
                GoogleDriveSetupError.EMAIL_RETRIEVAL_FAILED.toDriveSetupState()
            }
            is SuccessResult -> {
                settingsRepository.googleDriveAccountEmail = emailResult.value
                logger.info("Stored Google Drive account email: ${emailResult.value}")
                createDriveBackupFolder(repository)
            }
        }
    }

    private suspend fun createDriveBackupFolder(repository: IGoogleDriveRepository): GoogleDriveSetupState {
        when (val folderCreationRequired = isFolderCreationRequired(repository)) {
            is ErrorResult -> folderCreationRequired.error.toDriveSetupState()
            is SuccessResult -> {
                if (!folderCreationRequired.value) {
                    settingsRepository.googleDriveBackupEnabled = true
                    return GoogleDriveSetupState.Inactive
                }
            }
        }

        return when (val setupData = repository.createBackupFolder()) {
            is SuccessResult -> {
                settingsRepository.googleDriveBackupEnabled = true
                settingsRepository.googleDriveFolderId = setupData.value.folderId
                settingsRepository.googleDriveFolderName = setupData.value.folderName
                logger.info("Created Google Drive folder: ${setupData.value.folderName} (${setupData.value.folderId})")
                GoogleDriveSetupState.Inactive
            }
            is ErrorResult -> GoogleDriveSetupError.FOLDER_CREATION_FAILED.toDriveSetupState()
        }
    }

    private suspend fun isFolderCreationRequired(
        repository: IGoogleDriveRepository
    ): BinaryResult<Boolean, GoogleDriveSetupError> {
        val existingFolderId = settingsRepository.googleDriveFolderId
        val existingFolderName = settingsRepository.googleDriveFolderName

        if (existingFolderId.isEmpty() || existingFolderName.isEmpty()){
            return SuccessResult(true)
        }

        return repository.checkFolderAccess(existingFolderId, existingFolderName)
            .ifHasValue {
                if (it) {
                    logger.info("Reusing existing Google Drive folder: $existingFolderName ($existingFolderId)")
                } else {
                    logger.info("Stored Google Drive folder no longer accessible; creating a new one.")
                }
            }.mapError {
                logger.warning("Failed to check folder access", it)
                GoogleDriveSetupError.FOLDER_ACCESS_CHECK_FAILED
            }
    }

    fun resetDriveSetupState() {
        _driveSetupState.value = GoogleDriveSetupState.Inactive
    }

    fun disableGoogleDriveBackup() {
        settingsRepository.googleDriveBackupEnabled = false
        settingsRepository.googleDriveAccountEmail = ""
        // leave folderName and folderId intact to be able to use the same folder again
    }

    private fun MutableStateFlow<GoogleDriveSetupState>.withLoadingState(
        block: suspend () -> GoogleDriveSetupState
    ) {
        viewModelScope.launch {
            val newValue = try {
                value = GoogleDriveSetupState.Loading
                block()
            } catch(e: Exception) {
                logger.warning("Failed to handle Google Drive setup", e)
                GoogleDriveSetupError.UNKNOWN.toDriveSetupState()
            }

            logger.debug("Drive setup state changed to $newValue")
            when (newValue) {
                is GoogleDriveSetupState.ConsentRequired,
                is GoogleDriveSetupState.Inactive,
                is GoogleDriveSetupState.Loading -> Unit
                is GoogleDriveSetupState.Error -> disableGoogleDriveBackup()
            }

            value = newValue
        }
    }
}
