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
    private val messagesKey = stringPreferencesKey("pending_messages")
    private val json = Json { ignoreUnknownKeys = true }
    private val listSerializer = ListSerializer(BackupMessage.serializer())

    override suspend fun addMessage(message: BackupMessage) {
        try {
            context.backupMessageDataStore.edit { preferences ->
                val existing = parseMessages(preferences[messagesKey])
                val updated = existing + message
                preferences[messagesKey] = json.encodeToString(listSerializer, updated)
            }
            logger.debug("Persisted backup message: $message")
        } catch (e: Exception) {
            logger.error("Failed to persist backup message", e)
        }
    }

    override suspend fun getAndClearMessages(): List<BackupMessage> {
        return try {
            val messages = mutableListOf<BackupMessage>()
            context.backupMessageDataStore.edit { preferences ->
                val parsedMessages = parseMessages(preferences[messagesKey])
                    .sortedByDescending { it.timestamp }
                messages.addAll(parsedMessages)
                preferences.remove(messagesKey)
            }
            logger.debug("Read backup messages before clearing them: ${messages.size}")
            messages
        } catch (e: Exception) {
            logger.error("Failed to read backup messages", e)
            emptyList()
        }
    }

    override suspend fun clearMessages() {
        try {
            context.backupMessageDataStore.edit { preferences ->
                preferences.remove(messagesKey)
            }
        } catch (e: Exception) {
            logger.warning("Failed to clear backup messages", e)
        }
    }

    private fun parseMessages(raw: String?): List<BackupMessage> {
        return if (raw.isNullOrEmpty())
            emptyList()
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
