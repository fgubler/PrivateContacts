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
import ch.abwesend.privatecontacts.domain.model.importexport.DecryptionError
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveAuthResult
import ch.abwesend.privatecontacts.domain.model.importexport.googledrive.GoogleDriveFile
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.model.sync.SyncCommandEnvelope
import ch.abwesend.privatecontacts.domain.model.sync.SyncCursor
import ch.abwesend.privatecontacts.domain.model.sync.SyncSnapshot
import ch.abwesend.privatecontacts.domain.model.sync.SyncSnapshotIndex
import ch.abwesend.privatecontacts.domain.repository.IBackupMessageRepository
import ch.abwesend.privatecontacts.domain.repository.IEncryptionRepository
import ch.abwesend.privatecontacts.domain.repository.ISyncCursorRepository
import ch.abwesend.privatecontacts.domain.repository.ISyncOutboxRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveAuthorizationRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveRepository
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.settings.SettingsRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.backup.repository.BackupNotificationRepository
import ch.abwesend.privatecontacts.infrastructure.backup.util.WorkerErrorHandler
import ch.abwesend.privatecontacts.infrastructure.sync.SyncApplyService
import ch.abwesend.privatecontacts.infrastructure.sync.SyncEnvelopeMigrator
import ch.abwesend.privatecontacts.infrastructure.sync.SyncFileNames
import ch.abwesend.privatecontacts.infrastructure.sync.syncJson
import ch.abwesend.privatecontacts.view.screens.importexport.shared.ImportExportConstants.CRYPT_PRETENDING_MIME_TYPE
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.encodeToString
import java.io.File
import java.time.LocalDateTime
import java.util.UUID

/**
 * Lists the app-data folder (paged), downloads new command/batch files per per-device cursor,
 * decrypts and applies them, running bootstrap (snapshot) for a fresh device and gap-detection ->
 * reconciliation (issue 2) for a long-offline one. Best-effort snapshot writing bounds file count.
 */
class SyncPullWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    private val googleDriveAuthRepository: IGoogleDriveAuthorizationRepository by injectAnywhere()
    private val encryptionRepository: IEncryptionRepository by injectAnywhere()
    private val cursorRepository: ISyncCursorRepository by injectAnywhere()
    private val outboxRepository: ISyncOutboxRepository by injectAnywhere()
    private val applyService: SyncApplyService by injectAnywhere()
    private val backupMessageRepository: IBackupMessageRepository by injectAnywhere()
    private val backupNotificationRepository: BackupNotificationRepository by injectAnywhere()
    private val settingsRepository: SettingsRepository by injectAnywhere()

    private val migrator = SyncEnvelopeMigrator()

    companion object {
        private val errorHandler = WorkerErrorHandler()
        private const val SNAPSHOT_THRESHOLD = 500
        private const val COMPACTION_THRESHOLD = 50
    }

    private data class LogFile(val file: GoogleDriveFile, val parsed: SyncFileNames.ParsedLogFile)

    override suspend fun getForegroundInfo(): ForegroundInfo =
        backupNotificationRepository.createForegroundInfo()

    override suspend fun doWork(): Result {
        return errorHandler.doWorkWithErrorHandling(
            workDescription = "Contact sync pull",
            addPersistedErrorMessage = { textRes, args ->
                addMessage(applicationContext.getString(textRes, *args), BackupMessageSeverity.ERROR)
            }
        ) {
            val settings = Settings.nextOrDefault()
            if (!settings.syncEnabled) {
                logger.debug("Contact sync disabled, skipping pull")
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

            pull(driveRepository, passphrase, settings.syncDeviceId)
        }
    }

    private suspend fun pull(
        driveRepository: IGoogleDriveRepository,
        passphrase: String,
        myDeviceId: String,
    ): Result {
        val files = driveRepository.listAppDataFiles()
        val logFilesByDevice = files
            .mapNotNull { file -> SyncFileNames.parseLogFile(file.name)?.let { LogFile(file, it) } }
            .filter { it.parsed.deviceId != myDeviceId }
            .groupBy { it.parsed.deviceId }

        val cursors = cursorRepository.getAll().associate { it.deviceId to it.lastAppliedSequenceNo }
        val remoteCursors = cursors.filterKeys { it != myDeviceId }
        val needsBootstrap = remoteCursors.isEmpty() && logFilesByDevice.isNotEmpty()
        val gapDetected = logFilesByDevice.any { (device, deviceFiles) ->
            val cursor = cursors[device] ?: 0L
            val lowestSequence = deviceFiles.minOf { it.parsed.fromSequence }
            cursor > 0L && lowestSequence > cursor + 1L
        }

        if (needsBootstrap || gapDetected) {
            applySnapshotIfPresent(driveRepository, files, passphrase, reconcile = gapDetected && !needsBootstrap)
        }

        val effectiveCursors = cursorRepository.getAll().associate { it.deviceId to it.lastAppliedSequenceNo }
        tailReplay(driveRepository, passphrase, logFilesByDevice, effectiveCursors)

        settingsRepository.lastSyncDateTime = LocalDateTime.now()
        maybeWriteSnapshot(driveRepository, passphrase, files, myDeviceId)
        cleanupFoldedLogsAndOldSnapshots(driveRepository, passphrase)
        compactOwnLog(driveRepository, passphrase, myDeviceId)
        return Result.success()
    }

    private suspend fun applySnapshotIfPresent(
        driveRepository: IGoogleDriveRepository,
        files: List<GoogleDriveFile>,
        passphrase: String,
        reconcile: Boolean,
    ) {
        val snapshot = loadNewestSnapshot(driveRepository, files, passphrase)
        if (snapshot == null) {
            logger.warning("Bootstrap/gap-recovery requested but no snapshot available - relying on full log replay")
        } else {
            logger.info("Applying snapshot ${snapshot.snapshotId} (reconcile = $reconcile)")
            snapshot.contacts.forEach { snapshotContact ->
                if (reconcile) {
                    applyService.reconcileSnapshotContact(snapshotContact)
                } else {
                    applyService.insertSnapshotContact(snapshotContact)
                }
            }
            snapshot.perDeviceHighWater.forEach { (device, highWater) ->
                cursorRepository.upsert(SyncCursor(device, highWater, System.currentTimeMillis()))
            }
        }
    }

    private suspend fun tailReplay(
        driveRepository: IGoogleDriveRepository,
        passphrase: String,
        logFilesByDevice: Map<String, List<LogFile>>,
        cursors: Map<String, Long>,
    ) {
        logFilesByDevice.toSortedMap().forEach { (device, deviceFiles) ->
            val cursor = cursors[device] ?: 0L
            var maxApplied = cursor
            deviceFiles
                .sortedBy { it.parsed.fromSequence }
                .filter { it.parsed.toSequence > cursor }
                .forEach { logFile ->
                    val envelopes = downloadAndDecrypt(driveRepository, logFile.file, passphrase)
                    envelopes
                        .filter { it.uploadSequence > cursor }
                        .sortedBy { it.uploadSequence }
                        .forEach { envelope ->
                            val migrated = migrator.migrateOrNull(envelope)
                            if (migrated != null) {
                                applyService.applyEnvelope(migrated)
                                maxApplied = maxOf(maxApplied, envelope.uploadSequence)
                            }
                        }
                }
            if (maxApplied > cursor) {
                cursorRepository.upsert(SyncCursor(device, maxApplied, System.currentTimeMillis()))
            }
        }
    }

    private suspend fun downloadAndDecrypt(
        driveRepository: IGoogleDriveRepository,
        file: GoogleDriveFile,
        passphrase: String,
    ): List<SyncCommandEnvelope> {
        val ciphertext = downloadRaw(driveRepository, file) ?: return emptyList()
        val plaintext = when (val decrypted = encryptionRepository.decrypt(ciphertext, passphrase)) {
            is SuccessResult -> decrypted.value
            is ErrorResult -> {
                reportDecryptionError(decrypted.error, file.name)
                return emptyList()
            }
        }
        return parseEnvelopes(file.name, plaintext)
    }

    private fun parseEnvelopes(fileName: String, plaintext: String): List<SyncCommandEnvelope> =
        try {
            if (fileName.startsWith(SyncFileNames.BATCH_PREFIX)) {
                plaintext.lineSequence()
                    .filter { it.isNotBlank() }
                    .map { syncJson.decodeFromString<SyncCommandEnvelope>(it) }
                    .toList()
            } else {
                listOf(syncJson.decodeFromString<SyncCommandEnvelope>(plaintext))
            }
        } catch (e: Exception) {
            logger.error("Failed to parse sync payload $fileName", e)
            emptyList()
        }

    private suspend fun loadNewestSnapshot(
        driveRepository: IGoogleDriveRepository,
        files: List<GoogleDriveFile>,
        passphrase: String,
    ): SyncSnapshot? {
        val newestIndex = listSnapshotIndicesNewestFirst(driveRepository, files).firstOrNull()?.first
            ?: return null
        return loadSnapshotForIndex(driveRepository, files, newestIndex, passphrase)
    }

    /** the plaintext snapshot sidecars newest-first, paired with their Drive file (for deletion) */
    private suspend fun listSnapshotIndicesNewestFirst(
        driveRepository: IGoogleDriveRepository,
        files: List<GoogleDriveFile>,
    ): List<Pair<SyncSnapshotIndex, GoogleDriveFile>> =
        files
            .filter { it.name.startsWith(SyncFileNames.SNAPSHOT_INDEX_PREFIX) }
            .mapNotNull { file ->
                downloadRaw(driveRepository, file)?.let { raw ->
                    runCatching { syncJson.decodeFromString<SyncSnapshotIndex>(raw) }.getOrNull()?.let { it to file }
                }
            }
            .sortedByDescending { it.first.createdAtUtcMillis }

    private suspend fun loadSnapshotForIndex(
        driveRepository: IGoogleDriveRepository,
        files: List<GoogleDriveFile>,
        index: SyncSnapshotIndex,
        passphrase: String,
    ): SyncSnapshot? {
        val snapshotFile = files.firstOrNull { it.name == SyncFileNames.snapshotFileName(index.snapshotId) }
            ?: return null
        val ciphertext = downloadRaw(driveRepository, snapshotFile) ?: return null
        return when (val decrypted = encryptionRepository.decrypt(ciphertext, passphrase)) {
            is SuccessResult -> runCatching { syncJson.decodeFromString<SyncSnapshot>(decrypted.value) }.getOrNull()
            is ErrorResult -> {
                reportDecryptionError(decrypted.error, snapshotFile.name)
                null
            }
        }
    }

    /**
     * Grace-window cleanup: any device may delete any device's `cmd_`/`batch_` files folded at or
     * below the 2nd-newest snapshot's high-water (safe even if racy - the data lives in a snapshot),
     * and snapshots older than the newest two. Keeping the 2nd-newest ensures a device mid-bootstrap
     * against it is not stranded.
     */
    private suspend fun cleanupFoldedLogsAndOldSnapshots(
        driveRepository: IGoogleDriveRepository,
        passphrase: String,
    ) {
        try {
            val files = driveRepository.listAppDataFiles()
            val snapshots = listSnapshotIndicesNewestFirst(driveRepository, files)
            if (snapshots.size < 2) {
                return
            }

            val watermarkSnapshot = loadSnapshotForIndex(driveRepository, files, snapshots[1].first, passphrase)
                ?: return
            val watermark = watermarkSnapshot.perDeviceHighWater

            var deletedLogs = 0
            files.forEach { file ->
                val parsed = SyncFileNames.parseLogFile(file.name)
                val deviceWatermark = parsed?.let { watermark[it.deviceId] }
                if (parsed != null && deviceWatermark != null && parsed.toSequence <= deviceWatermark) {
                    if (driveRepository.deleteFile(file.id)) {
                        deletedLogs++
                    }
                }
            }

            snapshots.drop(2).forEach { (index, indexFile) ->
                files.firstOrNull { it.name == SyncFileNames.snapshotFileName(index.snapshotId) }
                    ?.let { driveRepository.deleteFile(it.id) }
                driveRepository.deleteFile(indexFile.id)
            }
            logger.info("Sync cleanup: deleted $deletedLogs folded log files and ${(snapshots.size - 2).coerceAtLeast(0)} old snapshots")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.warning("Failed to clean up old sync data", e)
        }
    }

    private suspend fun maybeWriteSnapshot(
        driveRepository: IGoogleDriveRepository,
        passphrase: String,
        files: List<GoogleDriveFile>,
        myDeviceId: String,
    ) {
        val logFileCount = files.count {
            it.name.startsWith(SyncFileNames.CMD_PREFIX) || it.name.startsWith(SyncFileNames.BATCH_PREFIX)
        }
        // the snapshot writer must fold only synced state, so skip while our own outbox is non-empty (issue 10)
        if (logFileCount <= SNAPSHOT_THRESHOLD || outboxRepository.count() > 0) {
            return
        }

        try {
            val highWater = files
                .mapNotNull { SyncFileNames.parseLogFile(it.name) }
                .groupBy { it.deviceId }
                .mapValues { entry -> entry.value.maxOf { it.toSequence } }
            val snapshotId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            val snapshot = applyService.buildSnapshot(snapshotId, myDeviceId, highWater, now)

            val snapshotJson = syncJson.encodeToString(snapshot)
            val ciphertext = when (val encrypted = encryptionRepository.encrypt(snapshotJson, passphrase)) {
                is SuccessResult -> encrypted.value
                is ErrorResult -> {
                    logger.error("Failed to encrypt snapshot", encrypted.error)
                    return
                }
            }
            uploadText(driveRepository, SyncFileNames.snapshotFileName(snapshotId), ciphertext)

            val index = SyncSnapshotIndex(snapshotId = snapshotId, createdByDevice = myDeviceId, createdAtUtcMillis = now)
            uploadText(driveRepository, SyncFileNames.snapshotIndexFileName(snapshotId), syncJson.encodeToString(index))
            logger.info("Wrote sync snapshot $snapshotId with ${snapshot.contacts.size} contacts")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.warning("Failed to write sync snapshot", e)
        }
    }

    private suspend fun uploadText(
        driveRepository: IGoogleDriveRepository,
        fileName: String,
        content: String,
    ): Boolean {
        val tempFile = File(applicationContext.cacheDir, fileName)
        return try {
            tempFile.writeText(content)
            driveRepository.uploadToAppData(tempFile, CRYPT_PRETENDING_MIME_TYPE) != null
        } finally {
            tempFile.delete()
        }
    }

    /**
     * Bounds the file count between snapshots: rewrites this device's own `cmd_` files into a single
     * encrypted JSONL `batch_`. Only own-device files are touched (no cross-device append race), and
     * the batch is uploaded and confirmed BEFORE the originals are deleted, so a partial failure never
     * loses commands (it leaves harmless duplicate representations that the cursor de-duplicates).
     */
    private suspend fun compactOwnLog(
        driveRepository: IGoogleDriveRepository,
        passphrase: String,
        myDeviceId: String,
    ) {
        try {
            val files = driveRepository.listAppDataFiles()
            val ownCommandFiles = files
                .filter { it.name.startsWith(SyncFileNames.CMD_PREFIX) }
                .mapNotNull { file ->
                    SyncFileNames.parseLogFile(file.name)
                        ?.takeIf { it.deviceId == myDeviceId }
                        ?.let { file to it.toSequence }
                }
                .sortedBy { it.second }
            if (ownCommandFiles.size <= COMPACTION_THRESHOLD) {
                return
            }

            val envelopeLines = mutableListOf<String>()
            for ((file, _) in ownCommandFiles) {
                val ciphertext = downloadRaw(driveRepository, file) ?: return // abort: do not risk data loss
                val plaintext = when (val decrypted = encryptionRepository.decrypt(ciphertext, passphrase)) {
                    is SuccessResult -> decrypted.value.trim()
                    is ErrorResult -> {
                        reportDecryptionError(decrypted.error, file.name)
                        return
                    }
                }
                envelopeLines.add(plaintext)
            }

            val fromSequence = ownCommandFiles.first().second
            val toSequence = ownCommandFiles.last().second
            val batchPlaintext = envelopeLines.joinToString(separator = "\n")
            val batchCiphertext = when (val encrypted = encryptionRepository.encrypt(batchPlaintext, passphrase)) {
                is SuccessResult -> encrypted.value
                is ErrorResult -> {
                    logger.error("Failed to encrypt compacted batch", encrypted.error)
                    return
                }
            }

            val batchName = SyncFileNames.batchFileName(myDeviceId, fromSequence, toSequence)
            if (!uploadText(driveRepository, batchName, batchCiphertext)) {
                logger.warning("Failed to upload compacted batch $batchName - keeping original command files")
                return
            }
            ownCommandFiles.forEach { (file, _) -> driveRepository.deleteFile(file.id) }
            logger.info("Compacted ${ownCommandFiles.size} command files into $batchName")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.warning("Failed to compact own sync log", e)
        }
    }

    private suspend fun downloadRaw(driveRepository: IGoogleDriveRepository, file: GoogleDriveFile): String? {
        val tempFile = File(applicationContext.cacheDir, file.name)
        return try {
            when (val downloaded = driveRepository.downloadFile(file.id, tempFile)) {
                is SuccessResult -> tempFile.readText()
                is ErrorResult -> {
                    logger.warning("Failed to download ${file.name}", downloaded.error)
                    null
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.warning("Failed to read downloaded ${file.name}", e)
            null
        } finally {
            tempFile.delete()
        }
    }

    private suspend fun reportDecryptionError(error: DecryptionError, fileName: String) {
        when (error) {
            DecryptionError.INVALID_PASSWORD -> {
                logger.warning("Wrong sync passphrase for $fileName")
                addMessage(
                    applicationContext.getString(R.string.sync_wrong_passphrase_error),
                    BackupMessageSeverity.ERROR,
                )
            }
            DecryptionError.INVALID_FILE,
            DecryptionError.UNKNOWN -> logger.warning("Failed to decrypt sync file $fileName ($error)")
        }
    }

    private suspend fun authorize() = when (val result = googleDriveAuthRepository.authorize()) {
        is GoogleDriveAuthResult.Authorized -> SuccessResult(result.data)
        is GoogleDriveAuthResult.ConsentRequired,
        is GoogleDriveAuthResult.Error -> {
            logger.warning("No Google authorization available, skipping sync pull")
            ErrorResult(Result.failure())
        }
    }

    private suspend fun addMessage(text: String, severity: BackupMessageSeverity) {
        backupMessageRepository.addDriveMessage(BackupMessage(text = text, severity = severity))
    }
}
