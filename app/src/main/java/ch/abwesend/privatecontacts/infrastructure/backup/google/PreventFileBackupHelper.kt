/*
 * Private Contacts
 * Copyright (c) 2024.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.backup.google

import android.app.backup.BackupDataOutput
import android.app.backup.FileBackupHelper
import android.content.Context
import android.os.ParcelFileDescriptor
import ch.abwesend.privatecontacts.domain.lib.logging.logger

/** We don't want to backup files at all. */
private val filesToBackup: Array<String> = emptyArray()

/**
 * FileBackupHelper to completely disable file-backup.
 * This is done by passing an empty array of files to back up.
 * The point is to prevent the backup of the SQLite database.
 */
class PreventFileBackupHelper(context: Context) : FileBackupHelper(context, *filesToBackup) {
    override fun performBackup(
        oldState: ParcelFileDescriptor?,
        data: BackupDataOutput?,
        newState: ParcelFileDescriptor?
    ) {
        logger.info("Preventing file backup.")
    }
}
