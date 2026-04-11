/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.backup

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.backup.BackupMessage
import ch.abwesend.privatecontacts.domain.repository.IBackupMessageRepository
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.backupMessageDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "backup_messages")

class BackupMessageRepository(private val context: Context) : IBackupMessageRepository {
    private val json = Json { ignoreUnknownKeys = true }
    private val listSerializer = ListSerializer(BackupMessage.serializer())

    private val localMessagesKey = stringPreferencesKey("pending_messages")
    private val driveMessagesKey = stringPreferencesKey("pending_drive_messages")

    override suspend fun addLocalMessage(message: BackupMessage) =
        addMessage(localMessagesKey, message)
    override suspend fun addDriveMessage(message: BackupMessage) =
        addMessage(driveMessagesKey, message)

    override suspend fun getAndClearLocalMessages(): List<BackupMessage> =
        getAndClearMessages(localMessagesKey)
    override suspend fun getAndClearDriveMessages(): List<BackupMessage> =
        getAndClearMessages(driveMessagesKey)

    override suspend fun clearLocalMessages() = clearMessages(localMessagesKey)
    override suspend fun clearDriveMessages() = clearMessages(driveMessagesKey)

    private suspend fun addMessage(key: Preferences.Key<String>, message: BackupMessage) {
        try {
            context.backupMessageDataStore.edit { preferences ->
                val existing = parseMessages(preferences[key])
                val updated = existing + message
                preferences[key] = json.encodeToString(listSerializer, updated)
            }
            logger.debug("Persisted backup message (${key.name}): $message")
        } catch (e: Exception) {
            logger.error("Failed to persist backup message (${key.name})", e)
        }
    }

    private suspend fun getAndClearMessages(key: Preferences.Key<String>): List<BackupMessage> {
        return try {
            val messages = mutableListOf<BackupMessage>()
            context.backupMessageDataStore.edit { preferences ->
                val parsedMessages = parseMessages(preferences[key])
                    .sortedByDescending { it.timestamp }
                messages.addAll(parsedMessages)
                preferences.remove(key)
            }
            logger.debug("Read backup messages (${key.name}) before clearing them: ${messages.size}")
            messages
        } catch (e: Exception) {
            logger.error("Failed to read backup messages (${key.name})", e)
            emptyList()
        }
    }

    private suspend fun clearMessages(key: Preferences.Key<String>) {
        try {
            context.backupMessageDataStore.edit { preferences ->
                preferences.remove(key)
            }
        } catch (e: Exception) {
            logger.warning("Failed to clear backup messages (${key.name})", e)
        }
    }

    private fun parseMessages(raw: String?): List<BackupMessage> {
        return if (raw.isNullOrEmpty()) emptyList()
        else {
            try {
                json.decodeFromString(listSerializer, raw)
            } catch (e: Exception) {
                logger.error("Failed to parse backup messages: $raw", e)
                emptyList()
            }
        }
    }
}
