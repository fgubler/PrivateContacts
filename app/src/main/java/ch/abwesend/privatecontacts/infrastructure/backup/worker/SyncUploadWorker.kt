/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.backup.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import ch.abwesend.privatecontacts.R
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.backup.BackupMessage
import ch.abwesend.privatecontacts.domain.model.backup.BackupMessageSeverity
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveAuthResult
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.model.sync.CreateContactCommand
import ch.abwesend.privatecontacts.domain.model.sync.DeleteContactCommand
import ch.abwesend.privatecontacts.domain.model.sync.SyncCommand
import ch.abwesend.privatecontacts.domain.model.sync.SyncCommandEnvelope
import ch.abwesend.privatecontacts.domain.model.sync.SyncCommandOperation
import ch.abwesend.privatecontacts.domain.model.sync.SyncCursor
import ch.abwesend.privatecontacts.domain.model.sync.SyncOutboxCommand
import ch.abwesend.privatecontacts.domain.model.sync.UpdateContactCommand
import ch.abwesend.privatecontacts.domain.repository.IBackupMessageRepository
import ch.abwesend.privatecontacts.domain.repository.IEncryptionRepository
import ch.abwesend.privatecontacts.domain.repository.ISyncCursorRepository
import ch.abwesend.privatecontacts.domain.repository.ISyncOutboxRepository
import ch.abwesend.privatecontacts.domain.repository.ISyncStateRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveAuthorizationRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.ISyncContactMapper
import ch.abwesend.privatecontacts.domain.service.interfaces.ISyncScheduler
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.settings.SettingsRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.backup.repository.BackupNotificationRepository
import ch.abwesend.privatecontacts.infrastructure.backup.util.WorkerErrorHandler
import ch.abwesend.privatecontacts.infrastructure.sync.SyncFileNames
import ch.abwesend.privatecontacts.infrastructure.sync.syncJson
import ch.abwesend.privatecontacts.view.screens.importexport.shared.ImportExportConstants.CRYPT_PRETENDING_MIME_TYPE
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.encodeToString
import java.io.File
import java.util.UUID

/**
 * Reads the outbox in order, coalesces superseded updates per contact, encrypts each command with
 * the sync passphrase, and uploads it as `cmd_<deviceId>_NNNNNN`. Then clears uploaded rows, bumps
 * `lastSyncedRevision`, and re-checks the outbox (self-re-enqueuing if new rows arrived - issue 4).
 */
class SyncUploadWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    private val googleDriveAuthRepository: IGoogleDriveAuthorizationRepository by injectAnywhere()
    private val encryptionRepository: IEncryptionRepository by injectAnywhere()
    private val outboxRepository: ISyncOutboxRepository by injectAnywhere()
    private val syncStateRepository: ISyncStateRepository by injectAnywhere()
    private val cursorRepository: ISyncCursorRepository by injectAnywhere()
    private val contactMapper: ISyncContactMapper by injectAnywhere()
    private val backupMessageRepository: IBackupMessageRepository by injectAnywhere()
    private val backupNotificationRepository: BackupNotificationRepository by injectAnywhere()
    private val syncScheduler: ISyncScheduler by injectAnywhere()
    private val settingsRepository: SettingsRepository by injectAnywhere()

    companion object {
        private val errorHandler = WorkerErrorHandler()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo =
        backupNotificationRepository.createForegroundInfo()

    override suspend fun doWork(): Result {
        return errorHandler.doWorkWithErrorHandling(
            workDescription = "Contact sync upload",
            addPersistedErrorMessage = { textRes, args ->
                addMessage(applicationContext.getString(textRes, *args), BackupMessageSeverity.ERROR)
            }
        ) {
            val settings = Settings.nextOrDefault()
            if (!settings.syncEnabled) {
                logger.debug("Contact sync disabled, skipping upload")
                return@doWorkWithErrorHandling Result.success()
            }

            val pending = outboxRepository.getPending()
            if (pending.isEmpty()) {
                logger.debug("Sync outbox empty, nothing to upload")
                return@doWorkWithErrorHandling Result.success()
            }

            val passphrase = when (val decrypted = encryptionRepository.decryptPassword(settings.syncPasswordEncrypted)) {
                is SuccessResult -> decrypted.value
                is ErrorResult -> {
                    logger.error("Failed to decrypt the sync passphrase", decrypted.error)
                    addMessage(
                        applicationContext.getString(R.string.sync_passphrase_unavailable_error),
                        BackupMessageSeverity.ERROR,
                    )
                    return@doWorkWithErrorHandling Result.failure()
                }
            }

            val driveRepository = when (val authorized = authorize()) {
                is SuccessResult -> authorized.value
                is ErrorResult -> return@doWorkWithErrorHandling authorized.error
            }

            val deviceId = resolveDeviceId()
            uploadPending(driveRepository, passphrase, deviceId, pending)
        }
    }

    private suspend fun uploadPending(
        driveRepository: IGoogleDriveRepository,
        passphrase: String,
        initialDeviceId: String,
        pending: List<SyncOutboxCommand>,
    ): Result {
        var deviceId = initialDeviceId
        val existingFiles = driveRepository.listAppDataFiles().map { it.name }.toSet()
        val listedMaxSequence = existingFiles.maxSequenceFor(deviceId)
        val recordedUploadSequence = cursorRepository.get(deviceId)?.lastAppliedSequenceNo ?: 0L

        // deviceId-collision liveness check (issue 6): another live device wrote to my namespace.
        if (listedMaxSequence > recordedUploadSequence && recordedUploadSequence > 0L) {
            logger.warning("Detected a foreign write in device-namespace $deviceId - minting a fresh deviceId")
            deviceId = UUID.randomUUID().toString()
            settingsRepository.syncDeviceId = deviceId
            cursorRepository.deleteAll()
        }

        var nextSequence = maxOf(existingFiles.maxSequenceFor(deviceId), recordedUploadSequence) + 1L

        // coalesce: keep only the latest command per contact (a later delete supersedes prior updates)
        val latestPerGuid = pending.groupBy { it.syncGuid }.mapValues { entry -> entry.value.last() }
        val coalesced = latestPerGuid.values.sortedBy { it.sequenceNo }
        val sequencesByGuid = pending.groupBy { it.syncGuid }.mapValues { entry -> entry.value.map { it.sequenceNo } }

        val sequencesToDelete = mutableListOf<Long>()
        var retryNeeded = false
        for (command in coalesced) {
            val fileName = SyncFileNames.commandFileName(deviceId, nextSequence)
            val uploaded = uploadCommand(driveRepository, passphrase, deviceId, nextSequence, fileName, command)
            if (uploaded) {
                markSynced(command)
                sequencesByGuid[command.syncGuid]?.let { sequencesToDelete.addAll(it) }
                nextSequence++
            } else {
                retryNeeded = true
                break
            }
        }

        if (sequencesToDelete.isNotEmpty()) {
            outboxRepository.delete(sequencesToDelete)
            cursorRepository.upsert(
                SyncCursor(
                    deviceId = deviceId,
                    lastAppliedSequenceNo = nextSequence - 1L,
                    updatedAtUtcMillis = System.currentTimeMillis(),
                )
            )
        }

        return when {
            retryNeeded -> Result.retry()
            // belt-and-suspenders: a save may have arrived during the run (issue 4)
            outboxRepository.count() > 0 -> {
                syncScheduler.scheduleUploadDebounced()
                Result.success()
            }
            else -> Result.success()
        }
    }

    private suspend fun uploadCommand(
        driveRepository: IGoogleDriveRepository,
        passphrase: String,
        deviceId: String,
        sequence: Long,
        fileName: String,
        command: SyncOutboxCommand,
    ): Boolean {
        return try {
            val envelope = SyncCommandEnvelope(
                commandId = command.commandId,
                deviceId = deviceId,
                uploadSequence = sequence,
                contactSyncId = command.syncGuid,
                baseRevision = command.baseRevision,
                newRevision = command.newRevision,
                changeTimestampUtcMillis = command.createdAtUtcMillis,
                command = command.toSyncCommand(),
            )
            val envelopeJson = syncJson.encodeToString(envelope)
            val ciphertext = when (val encrypted = encryptionRepository.encrypt(envelopeJson, passphrase)) {
                is SuccessResult -> encrypted.value
                is ErrorResult -> {
                    logger.error("Failed to encrypt sync command ${command.commandId}", encrypted.error)
                    return false
                }
            }

            val tempFile = File(applicationContext.cacheDir, fileName)
            try {
                tempFile.writeText(ciphertext)
                driveRepository.uploadToAppData(tempFile, CRYPT_PRETENDING_MIME_TYPE) != null
            } finally {
                tempFile.delete()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error("Failed to upload sync command ${command.commandId}", e)
            false
        }
    }

    private suspend fun markSynced(command: SyncOutboxCommand) {
        val state = syncStateRepository.getBySyncGuid(command.syncGuid) ?: return
        val stillDirty = state.localRevision != command.newRevision
        syncStateRepository.upsert(
            state.copy(
                lastSyncedRevision = command.newRevision,
                dirty = stillDirty,
                deleteSyncedAtUtcMillis = if (command.operation == SyncCommandOperation.DELETE) {
                    System.currentTimeMillis()
                } else {
                    state.deleteSyncedAtUtcMillis
                },
            )
        )
    }

    private fun SyncOutboxCommand.toSyncCommand(): SyncCommand =
        when (operation) {
            SyncCommandOperation.CREATE -> CreateContactCommand(contactMapper.deserializeContact(plaintextPayload))
            SyncCommandOperation.UPDATE -> UpdateContactCommand(contactMapper.deserializeContact(plaintextPayload))
            SyncCommandOperation.DELETE -> DeleteContactCommand(syncGuid)
        }

    private fun Set<String>.maxSequenceFor(deviceId: String): Long =
        mapNotNull { SyncFileNames.parseLogFile(it) }
            .filter { it.deviceId == deviceId }
            .maxOfOrNull { it.toSequence }
            ?: 0L

    private fun resolveDeviceId(): String {
        val existing = settingsRepository.syncDeviceId
        return if (existing.isEmpty()) {
            val newDeviceId = UUID.randomUUID().toString()
            settingsRepository.syncDeviceId = newDeviceId
            newDeviceId
        } else {
            existing
        }
    }

    private suspend fun authorize() = when (val result = googleDriveAuthRepository.authorize()) {
        is GoogleDriveAuthResult.Authorized -> SuccessResult(result.data)
        is GoogleDriveAuthResult.ConsentRequired,
        is GoogleDriveAuthResult.Error -> {
            logger.warning("No Google authorization available, skipping sync upload")
            addMessage(
                applicationContext.getString(R.string.drive_backup_account_not_signed_in_error),
                BackupMessageSeverity.ERROR,
            )
            ErrorResult(Result.failure())
        }
    }

    private suspend fun addMessage(text: String, severity: BackupMessageSeverity) {
        backupMessageRepository.addDriveMessage(BackupMessage(text = text, severity = severity))
    }
}
