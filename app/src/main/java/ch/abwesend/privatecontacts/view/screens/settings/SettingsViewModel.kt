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
import ch.abwesend.privatecontacts.domain.model.sync.SyncIntermediateSetupState
import ch.abwesend.privatecontacts.domain.model.sync.SyncSetupError
import ch.abwesend.privatecontacts.domain.model.sync.SyncSetupState
import ch.abwesend.privatecontacts.domain.model.sync.toSyncSetupState
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.repository.IEncryptionRepository
import ch.abwesend.privatecontacts.domain.repository.ISyncConflictRepository
import ch.abwesend.privatecontacts.domain.repository.ISyncCursorRepository
import ch.abwesend.privatecontacts.domain.repository.ISyncOutboxRepository
import ch.abwesend.privatecontacts.domain.repository.ISyncStateRepository
import ch.abwesend.privatecontacts.domain.repository.KeyStorePurpose
import ch.abwesend.privatecontacts.domain.service.ContactSaveService
import ch.abwesend.privatecontacts.domain.service.DatabaseService
import ch.abwesend.privatecontacts.domain.service.GoogleDriveSetupService
import ch.abwesend.privatecontacts.domain.service.LauncherAppearanceService
import ch.abwesend.privatecontacts.domain.service.SyncResetService
import ch.abwesend.privatecontacts.domain.service.SyncSetupService
import ch.abwesend.privatecontacts.domain.service.interfaces.IBackupScheduler
import ch.abwesend.privatecontacts.domain.service.interfaces.ISyncScheduler
import ch.abwesend.privatecontacts.domain.service.interfaces.PermissionService
import ch.abwesend.privatecontacts.domain.settings.SettingsRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.view.screens.sync.SyncConflictUiModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID

class SettingsViewModel : ViewModel() {
    private val databaseService: DatabaseService by injectAnywhere()
    private val launcherAppearanceService: LauncherAppearanceService by injectAnywhere()
    private val permissionService: PermissionService by injectAnywhere()
    private val backupScheduler: IBackupScheduler by injectAnywhere()
    private val encryptionRepository: IEncryptionRepository by injectAnywhere()
    private val settingsRepository: SettingsRepository by injectAnywhere()
    private val driveSetupService: GoogleDriveSetupService by injectAnywhere()
    private val syncSetupService: SyncSetupService by injectAnywhere()
    private val syncResetService: SyncResetService by injectAnywhere()
    private val syncScheduler: ISyncScheduler by injectAnywhere()
    private val syncCursorRepository: ISyncCursorRepository by injectAnywhere()
    private val syncOutboxRepository: ISyncOutboxRepository by injectAnywhere()
    private val syncStateRepository: ISyncStateRepository by injectAnywhere()
    private val syncConflictRepository: ISyncConflictRepository by injectAnywhere()
    private val contactRepository: IContactRepository by injectAnywhere()
    private val contactSaveService: ContactSaveService by injectAnywhere()

    private val _driveSetupState =
        MutableStateFlow<GoogleDriveSetupState>(GoogleDriveSetupState.Inactive)
    val driveSetupState: StateFlow<GoogleDriveSetupState> = _driveSetupState.asStateFlow()

    private val _syncSetupState = MutableStateFlow<SyncSetupState>(SyncSetupState.Inactive)
    val syncSetupState: StateFlow<SyncSetupState> = _syncSetupState.asStateFlow()

    private val _syncResetInProgress = MutableStateFlow(false)
    val syncResetInProgress: StateFlow<Boolean> = _syncResetInProgress.asStateFlow()

    /** the passphrase captured before authorization, needed again after a consent round-trip */
    private var pendingSyncPassphrase: String? = null

    val syncConflictCount: StateFlow<Int> = syncConflictRepository.getAllAsFlow()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val syncConflicts: StateFlow<List<SyncConflictUiModel>> = syncConflictRepository.getAllAsFlow()
        .map { conflicts ->
            conflicts.map { conflict ->
                SyncConflictUiModel(
                    syncId = conflict.syncId,
                    originalName = conflict.localContactId?.let { resolveDisplayName(it) },
                    copyName = resolveDisplayName(conflict.forkContactId).orEmpty(),
                    localContactId = conflict.localContactId,
                    forkContactId = conflict.forkContactId,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private suspend fun resolveDisplayName(contactId: ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal): String? =
        try {
            contactRepository.resolveContact(contactId).displayName
        } catch (e: Exception) {
            logger.warning("Failed to resolve conflict contact $contactId", e)
            null
        }

    fun markConflictResolved(syncId: String) {
        viewModelScope.launch { syncConflictRepository.resolve(syncId) }
    }

    fun deleteConflictCopy(model: SyncConflictUiModel) {
        viewModelScope.launch {
            try {
                val contact = contactRepository.resolveContact(model.forkContactId)
                contactSaveService.deleteContact(contact)
            } catch (e: Exception) {
                logger.warning("Failed to delete conflict copy ${model.forkContactId}", e)
            }
            syncConflictRepository.resolve(model.syncId)
        }
    }

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
        when (val result = encryptionRepository.encryptPassword(password, KeyStorePurpose.BACKUP)) {
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
        encryptionRepository.deleteKeyStoreKey(KeyStorePurpose.BACKUP)
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

    fun enableSyncWithPassphrase(passphrase: String) {
        when (val result = encryptionRepository.encryptPassword(passphrase, KeyStorePurpose.SYNC)) {
            is SuccessResult -> {
                settingsRepository.syncPasswordEncrypted = result.value
                if (settingsRepository.syncDeviceId.isEmpty()) {
                    settingsRepository.syncDeviceId = UUID.randomUUID().toString()
                }
                pendingSyncPassphrase = passphrase
                _syncSetupState.withSyncLoadingState { syncSetupService.enableSync(passphrase) }
            }
            is ErrorResult -> {
                logger.warning("Failed to encrypt sync passphrase", result.error)
                _syncSetupState.value = SyncSetupError.UNKNOWN.toSyncSetupState()
            }
        }
    }

    fun handleSyncConsentResponse(data: Intent?) {
        val passphrase = pendingSyncPassphrase
        if (passphrase == null) {
            _syncSetupState.value = SyncSetupError.UNKNOWN.toSyncSetupState()
        } else {
            _syncSetupState.withSyncLoadingState { syncSetupService.handleConsentResponse(data, passphrase) }
        }
    }

    fun resetSyncSetupState() {
        _syncSetupState.value = SyncSetupState.Inactive
    }

    fun disableSync() {
        settingsRepository.syncEnabled = false
        // leave the remote data in place; clear only local cursors/outbox so a re-enable bootstraps cleanly
        viewModelScope.launch {
            syncCursorRepository.deleteAll()
            syncOutboxRepository.deleteAll()
        }
    }

    fun triggerSyncNow() {
        syncScheduler.triggerPullNow()
        syncScheduler.scheduleUploadDebounced()
    }

    /**
     * Fully resets synchronization: wipes all remote data from Google Drive and clears every local
     * sync trace (passphrase, account, cursors, outbox, per-contact state, conflicts and the sync
     * KeyStore key). Local contacts are left untouched. This is destructive - the previously
     * synchronized history is not recoverable afterwards.
     */
    fun resetSync() {
        viewModelScope.launch {
            _syncResetInProgress.value = true
            try {
                when (val result = syncResetService.deleteAllRemoteSyncData()) {
                    is SuccessResult -> logger.info("Deleted ${result.value} remote sync files during reset")
                    is ErrorResult -> logger.warning("Failed to wipe remote sync data during reset", result.error)
                }
            } catch (exception: Exception) {
                logger.warning("Failed to wipe remote sync data during reset", exception)
            }

            settingsRepository.syncEnabled = false
            settingsRepository.syncPasswordEncrypted = ""
            settingsRepository.syncAccountId = ""
            settingsRepository.lastSyncDateTime = LocalDateTime.MIN
            encryptionRepository.deleteKeyStoreKey(KeyStorePurpose.SYNC)
            syncStateRepository.deleteAll()
            syncCursorRepository.deleteAll()
            syncOutboxRepository.deleteAll()
            syncConflictRepository.deleteAll()
            _syncResetInProgress.value = false
        }
    }

    private fun MutableStateFlow<SyncSetupState>.withSyncLoadingState(
        block: suspend () -> SyncIntermediateSetupState
    ) {
        viewModelScope.launch {
            val newValue = try {
                value = SyncSetupState.Loading
                block()
            } catch (e: Exception) {
                logger.warning("Failed to enable contact sync", e)
                SyncSetupError.UNKNOWN.toSyncSetupState()
            }

            value = when (newValue) {
                is SyncSetupState.ConsentRequired,
                is SyncSetupState.Inactive,
                is SyncSetupState.Loading -> newValue
                is SyncSetupState.Error -> {
                    settingsRepository.syncEnabled = false
                    settingsRepository.syncPasswordEncrypted = ""
                    pendingSyncPassphrase = null
                    newValue
                }
                is SyncIntermediateSetupState.Success -> {
                    settingsRepository.syncEnabled = true
                    settingsRepository.syncAccountId = newValue.accountEmail
                    pendingSyncPassphrase = null
                    syncScheduler.triggerPullNow()
                    SyncSetupState.Inactive
                }
            }
        }
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
