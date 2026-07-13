/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.sync

import kotlinx.serialization.Serializable

/**
 * Full-state checkpoint. The most PII-dense object and therefore never written in cleartext.
 * [perDeviceHighWater] ("up to seq N from device D is folded in") is an immutable property of the
 * snapshot and lives inside the encrypted payload, because the consumer is a fresh device with no
 * local state.
 */
@Serializable
data class SyncSnapshot(
    val protocolVersion: Int = CURRENT_PROTOCOL_VERSION,
    val snapshotId: String,
    val createdByDevice: String,
    val createdAtUtcMillis: Long,
    val perDeviceHighWater: Map<String, Long>,
    val contacts: List<SyncSnapshotContact>,
)

/** a contact plus its revision as folded into a [SyncSnapshot] */
@Serializable
data class SyncSnapshotContact(
    val contact: SyncContact,
    val revision: String,
)

/** tiny encrypted passphrase verifier written on first enable */
@Serializable
data class SyncKeyCheck(
    val protocolVersion: Int = CURRENT_PROTOCOL_VERSION,
    val magic: String = KEYCHECK_MAGIC,
) {
    companion object {
        const val KEYCHECK_MAGIC = "PrivateContactsSyncKeyCheck"
    }
}
