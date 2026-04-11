/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.repository

import ch.abwesend.privatecontacts.domain.model.backup.BackupMessage

interface IBackupMessageRepository {
    suspend fun addLocalMessage(message: BackupMessage)
    suspend fun getAndClearLocalMessages(): List<BackupMessage>
    suspend fun clearLocalMessages()

    suspend fun addDriveMessage(message: BackupMessage)
    suspend fun getAndClearDriveMessages(): List<BackupMessage>
    suspend fun clearDriveMessages()
}
