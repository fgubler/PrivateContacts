package ch.abwesend.privatecontacts.infrastructure.backup.google

import android.app.backup.BackupAgentHelper
import android.app.backup.BackupDataInput
import android.app.backup.BackupDataOutput
import android.app.backup.FullBackupDataOutput
import android.os.ParcelFileDescriptor
import ch.abwesend.privatecontacts.domain.lib.logging.error
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.settings.SettingsRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private const val LOGGING_PREFIX = "Custom backup:"
class CustomBackupAgent : BackupAgentHelper() {
    private val settingsRepository: SettingsRepository by injectAnywhere()

    override fun onBackup(oldState: ParcelFileDescriptor?, data: BackupDataOutput?, newState: ParcelFileDescriptor?) {
        if (shouldPerformBackup()) {
            logger.info("$LOGGING_PREFIX creating backup")
            super.onBackup(oldState, data, newState)
        }
    }

    override fun onFullBackup(data: FullBackupDataOutput?) {
        if (shouldPerformBackup()) {
            logger.info("$LOGGING_PREFIX creating full backup")
            super.onFullBackup(data)
        }
    }

    override fun onRestore(data: BackupDataInput?, appVersionCode: Int, newState: ParcelFileDescriptor?) {
        super.onRestore(data, appVersionCode, newState)
        logger.info("$LOGGING_PREFIX restoring backup")
    }

    override fun onQuotaExceeded(backupDataBytes: Long, quotaBytes: Long) {
        super.onQuotaExceeded(backupDataBytes, quotaBytes)
        logger.error("$LOGGING_PREFIX backup-Quota of $quotaBytes bytes exceeded: needed $backupDataBytes bytes")
    }

    private fun shouldPerformBackup(): Boolean =
        try {
            logger.info("$LOGGING_PREFIX checking settings to decide whether to perform Google backup")
            val settings = runBlocking { settingsRepository.settings.first() }
            if (settings.useGoogleBackup) {
                logger.info("$LOGGING_PREFIX checked settings: proceeding with Google backup")
                true
            } else {
                logger.info("$LOGGING_PREFIX checked settings: skipping Google backup")
                false
            }
        } catch (e: Exception) {
            logger.error("$LOGGING_PREFIX failed to perform backup (or determine whether we should)", e)
            false
        }
}
