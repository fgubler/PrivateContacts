/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.sync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Per-contact CRUD command. Carried inside a [SyncCommandEnvelope] and applied on remote devices.
 */
@Serializable
sealed interface SyncCommand

@Serializable
@SerialName("create")
data class CreateContactCommand(val contact: SyncContact) : SyncCommand

@Serializable
@SerialName("update")
data class UpdateContactCommand(val contact: SyncContact) : SyncCommand

@Serializable
@SerialName("delete")
data class DeleteContactCommand(val contactSyncId: String) : SyncCommand

enum class SyncCommandOperation { CREATE, UPDATE, DELETE }
