/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.sync

import android.content.Context
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.importexport.DecryptionError
import ch.abwesend.privatecontacts.domain.model.result.generic.ErrorResult
import ch.abwesend.privatecontacts.domain.model.result.generic.SuccessResult
import ch.abwesend.privatecontacts.domain.model.sync.SyncKeyCheck
import ch.abwesend.privatecontacts.domain.model.sync.SyncProtocolMetadata
import ch.abwesend.privatecontacts.domain.repository.IEncryptionRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.IGoogleDriveRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.ISyncKeyCheckService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.view.screens.importexport.shared.ImportExportConstants.CRYPT_PRETENDING_MIME_TYPE
import kotlinx.serialization.encodeToString
import java.io.File

class SyncKeyCheckService(private val context: Context) : ISyncKeyCheckService {
    private val encryptionRepository: IEncryptionRepository by injectAnywhere()

    override suspend fun verifyOrInitialize(
        driveRepository: IGoogleDriveRepository,
        passphrase: String,
    ): ISyncKeyCheckService.Outcome {
        val files = driveRepository.listAppDataFiles()
        val existingKeyCheck = files.firstOrNull { it.name == SyncFileNames.KEYCHECK_FILE }
        return if (existingKeyCheck == null) {
            initialize(driveRepository, passphrase)
        } else {
            verify(driveRepository, existingKeyCheck.id, existingKeyCheck.name, passphrase)
        }
    }

    private suspend fun verify(
        driveRepository: IGoogleDriveRepository,
        fileId: String,
        fileName: String,
        passphrase: String,
    ): ISyncKeyCheckService.Outcome {
        val tempFile = File(context.cacheDir, fileName)
        return try {
            when (val downloaded = driveRepository.downloadFile(fileId, tempFile)) {
                is ErrorResult -> {
                    logger.warning("Failed to download keycheck", downloaded.error)
                    ISyncKeyCheckService.Outcome.ERROR
                }
                is SuccessResult -> {
                    when (val decrypted = encryptionRepository.decrypt(tempFile.readText(), passphrase)) {
                        is SuccessResult -> ISyncKeyCheckService.Outcome.MATCH
                        is ErrorResult ->
                            if (decrypted.error == DecryptionError.INVALID_PASSWORD) {
                                ISyncKeyCheckService.Outcome.MISMATCH
                            } else {
                                logger.warning("Keycheck decryption failed: ${decrypted.error}")
                                ISyncKeyCheckService.Outcome.ERROR
                            }
                    }
                }
            }
        } finally {
            tempFile.delete()
        }
    }

    private suspend fun initialize(
        driveRepository: IGoogleDriveRepository,
        passphrase: String,
    ): ISyncKeyCheckService.Outcome {
        val keyCheckJson = syncJson.encodeToString(SyncKeyCheck())
        val ciphertext = when (val encrypted = encryptionRepository.encrypt(keyCheckJson, passphrase)) {
            is SuccessResult -> encrypted.value
            is ErrorResult -> {
                logger.error("Failed to encrypt keycheck", encrypted.error)
                return ISyncKeyCheckService.Outcome.ERROR
            }
        }
        val uploaded = uploadText(driveRepository, SyncFileNames.KEYCHECK_FILE, ciphertext)
        if (uploaded) {
            val protocol = SyncProtocolMetadata(createdAtUtcMillis = System.currentTimeMillis())
            uploadText(driveRepository, SyncFileNames.PROTOCOL_FILE, syncJson.encodeToString(protocol))
        }
        return if (uploaded) ISyncKeyCheckService.Outcome.INITIALIZED else ISyncKeyCheckService.Outcome.ERROR
    }

    private suspend fun uploadText(
        driveRepository: IGoogleDriveRepository,
        fileName: String,
        content: String,
    ): Boolean {
        val tempFile = File(context.cacheDir, fileName)
        return try {
            tempFile.writeText(content)
            driveRepository.uploadToAppData(tempFile, CRYPT_PRETENDING_MIME_TYPE) != null
        } finally {
            tempFile.delete()
        }
    }
}
