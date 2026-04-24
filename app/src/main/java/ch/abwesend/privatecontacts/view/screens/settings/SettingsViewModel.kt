package ch.abwesend.privatecontacts.view.screens.settings

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveIntermediateSetupState
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveSetupError
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveSetupState
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.toDriveSetupState
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.repository.IEncryptionRepository
import ch.abwesend.privatecontacts.domain.service.DatabaseService
import ch.abwesend.privatecontacts.domain.service.GoogleDriveSetupService
import ch.abwesend.privatecontacts.domain.service.LauncherAppearanceService
import ch.abwesend.privatecontacts.domain.service.interfaces.IBackupScheduler
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
    private val driveSetupService: GoogleDriveSetupService by injectAnywhere()

    private val _driveSetupState =
        MutableStateFlow<GoogleDriveSetupState>(GoogleDriveSetupState.Inactive)
    val driveSetupState: StateFlow<GoogleDriveSetupState> = _driveSetupState.asStateFlow()

    fun initialize(settingsRepository: SettingsRepository) {
        if (!permissionService.hasContactReadPermission()) {
            settingsRepository.blockIncomingCallsFromUnknownNumbers = false
            settingsRepository.showAndroidContacts = false
            settingsRepository.defaultContactType = ContactType.Companion.default
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

    fun encryptAndSaveBackupPassword(password: String) {
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
            driveSetupService.requestGoogleDriveAuthorization()
        }
    }

    fun handleGoogleDriveConsentResponse(data: Intent?) {
        _driveSetupState.withLoadingState {
            driveSetupService.handleGoogleDriveConsentResponse(data)
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
        block: suspend () -> GoogleDriveIntermediateSetupState
    ) {
        viewModelScope.launch {
            val newValue = try {
                value = GoogleDriveSetupState.Loading
                block()
            } catch (e: Exception) {
                logger.warning("Failed to handle Google Drive setup", e)
                GoogleDriveSetupError.UNKNOWN.toDriveSetupState()
            }

            logger.debug("Drive setup state changed to $newValue")
            val mappedValue = when (newValue) {
                is GoogleDriveSetupState.ConsentRequired,
                is GoogleDriveSetupState.Inactive,
                is GoogleDriveSetupState.Loading -> newValue
                is GoogleDriveSetupState.Error -> {
                    disableGoogleDriveBackup()
                    newValue
                }
                is GoogleDriveIntermediateSetupState.Success -> {
                    settingsRepository.googleDriveBackupEnabled = newValue.backupEnabled
                    settingsRepository.googleDriveAccountEmail = newValue.accountEmail
                    settingsRepository.googleDriveFolderName = newValue.folderName
                    settingsRepository.googleDriveFolderId = newValue.folderId
                    GoogleDriveSetupState.Inactive
                }
            }

            value = mappedValue
        }
    }
}
