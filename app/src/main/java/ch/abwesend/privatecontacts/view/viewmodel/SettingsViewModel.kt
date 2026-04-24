/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.view.viewmodel

import android.app.PendingIntent
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveAuthResult
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
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    private val databaseService: DatabaseService by injectAnywhere()
    private val launcherAppearanceService: LauncherAppearanceService by injectAnywhere()
    private val permissionService: PermissionService by injectAnywhere()
    private val backupScheduler: IBackupScheduler by injectAnywhere()
    private val encryptionRepository: IEncryptionRepository by injectAnywhere()
    private val settingsRepository: SettingsRepository by injectAnywhere()
    private val googleDriveAuthRepository: IGoogleDriveAuthenticationRepository by injectAnywhere()

    // TODO extend to proper progress-construct including error-dialog
    private val _driveSetupInProgress = MutableStateFlow(false)
    val driveSetupInProgress: StateFlow<Boolean> = _driveSetupInProgress

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

    fun requestGoogleDriveAuthorization(onPendingIntent: (PendingIntent) -> Unit) {
        viewModelScope.launch {
            _driveSetupInProgress.value = true
            try {
                val result = googleDriveAuthRepository.authorize()
                handleGoogleDriveAuthorizationResult(result, onPendingIntent)
            } finally {
                _driveSetupInProgress.value = false
            }
        }
    }

    private suspend fun handleGoogleDriveAuthorizationResult(
        result: GoogleDriveAuthResult<IGoogleDriveRepository>,
        onPendingIntent: (PendingIntent) -> Unit,
    ) {
        when (result) {
            is GoogleDriveAuthResult.Authorized -> {
                onGoogleDriveAuthorized(result.data)
            }
            is GoogleDriveAuthResult.ConsentRequired ->
                onPendingIntent(result.pendingIntent) // TODO consider async mechanism here
            is GoogleDriveAuthResult.Error -> {
                settingsRepository.googleDriveBackupEnabled = false
            }
        }
    }

    fun handleGoogleDriveConsentResponse(data: Intent?) {
        viewModelScope.launch {
            _driveSetupInProgress.value = true
            try {
                when (val authResult = googleDriveAuthRepository.authorizeFromIntent(data)) {
                    is ErrorResult -> TODO("show error message")
                    is SuccessResult -> onGoogleDriveAuthorized(authResult.value)
                }
            } finally {
                _driveSetupInProgress.value = false
            }
        }
    }

    private suspend fun isFolderCreationRequired(repository: IGoogleDriveRepository): Boolean {
        val existingFolderId = settingsRepository.googleDriveFolderId
        val existingFolderName = settingsRepository.googleDriveFolderName

        if (existingFolderId.isEmpty() || existingFolderName.isEmpty()) return true

        return when (val accessResult = repository.checkFolderAccess(existingFolderId, existingFolderName)) {
            is SuccessResult -> {
                if (accessResult.value) {
                    logger.info("Reusing existing Google Drive folder: $existingFolderName ($existingFolderId)")
                    false
                } else {
                    logger.info("Stored Google Drive folder no longer accessible; creating a new one.")
                    true
                }
            }
            is ErrorResult -> {
                logger.warning("Failed to check folder access", accessResult.error)
                true
            }
        }
    }

    private suspend fun onGoogleDriveAuthorized(repository: IGoogleDriveRepository) {
        storeAccountEmail(repository)

        if (!isFolderCreationRequired(repository)) {
            settingsRepository.googleDriveBackupEnabled = true
            return
        }

        when (val setupData = repository.createBackupFolder()) {
            is SuccessResult -> {
                settingsRepository.googleDriveBackupEnabled = true
                settingsRepository.googleDriveFolderId = setupData.value.folderId
                settingsRepository.googleDriveFolderName = setupData.value.folderName
                logger.info("Created Google Drive folder: ${setupData.value.folderName} (${setupData.value.folderId})")
            }
            is ErrorResult -> {
                settingsRepository.googleDriveBackupEnabled = false
                // TODO show error dialog
                logger.warning("Failed to create Google Drive folder", setupData.error)
            }
        }
    }

    private suspend fun storeAccountEmail(repository: IGoogleDriveRepository) {
        when (val result = repository.getAccountEmail()) {
            is SuccessResult -> {
                settingsRepository.googleDriveAccountEmail = result.value
                logger.info("Stored Google Drive account email: ${result.value}")
            }
            is ErrorResult -> logger.warning("Failed to retrieve Google Drive account email", result.error)
        }
    }

    fun disableGoogleDriveBackup() {
        settingsRepository.googleDriveBackupEnabled = false
        settingsRepository.googleDriveAccountEmail = ""
        // leave folderName and folderId intact to be able to use the same folder again
    }
}
