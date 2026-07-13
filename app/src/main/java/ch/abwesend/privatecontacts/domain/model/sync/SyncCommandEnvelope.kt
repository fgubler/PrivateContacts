/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.sync

import kotlinx.serialization.Serializable

/**
 * The unit that is serialized to JSON and then encrypted before being uploaded to Drive.
 * Ordering & dedup rely on [deviceId] + [uploadSequence] only, never on wall-clock time.
 */
@Serializable
data class SyncCommandEnvelope(
    val protocolVersion: Int = CURRENT_PROTOCOL_VERSION,
    val commandId: String,
    val deviceId: String,
    val uploadSequence: Long,
    val contactSyncId: String,
    /** revision the editor saw before the change (null for Create) */
    val baseRevision: String?,
    /** revision this command establishes */
    val newRevision: String,
    /** display/tiebreak only - never used for ordering */
    val changeTimestampUtcMillis: Long,
    val command: SyncCommand,
)
