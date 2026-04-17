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
                when (val result = googleDriveAuthRepository.authorize()) {
                    is GoogleDriveAuthResult.Authorized -> {
                        val setupData = result.data.createBackupFolder()
                        when (setupData) {
                            is SuccessResult -> {
                                settingsRepository.googleDriveBackupEnabled = true
                                settingsRepository.googleDriveFolderId = setupData.value.folderId
                                settingsRepository.googleDriveFolderName = setupData.value.folderName
                                settingsRepository.googleDriveAccountEmail = setupData.value.accountEmail
                            }
                            else -> settingsRepository.googleDriveBackupEnabled = false
                        }
                    }
                    is GoogleDriveAuthResult.ConsentRequired -> onPendingIntent(result.pendingIntent)
                    is GoogleDriveAuthResult.Error -> {
                        settingsRepository.googleDriveBackupEnabled = false
                    }
                }
            } finally {
                _driveSetupInProgress.value = false
            }
        }
    }

    fun handleGoogleAuthorizationResult(data: Intent?) {
        viewModelScope.launch {
            _driveSetupInProgress.value = true
            try {
                val authResult = googleDriveAuthRepository.authorizeFromIntent(data)
                if (authResult is SuccessResult) {
                    val setupData = authResult.value.createBackupFolder()
                    // TODO handle setupData result
                }
            } finally {
                _driveSetupInProgress.value = false
            }
        }
    }

    fun disableGoogleDriveBackup() {
        settingsRepository.googleDriveBackupEnabled = false
        settingsRepository.googleDriveAccountEmail = ""
        settingsRepository.googleDriveFolderId = ""
        settingsRepository.googleDriveFolderName = ""
    }
}
