/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.repository

import ch.abwesend.privatecontacts.domain.model.backup.BackupMessage

interface IBackupMessageRepository {
    suspend fun addMessage(message: BackupMessage)
    suspend fun getAndClearMessages(): List<BackupMessage>
    suspend fun clearMessages()
}
