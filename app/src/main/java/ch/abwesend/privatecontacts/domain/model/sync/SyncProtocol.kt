/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.sync

import kotlinx.serialization.Serializable

/**
 * Protocol version carried by every envelope/snapshot. A reader refuses files with
 * [SyncCommandEnvelope.protocolVersion] greater than [CURRENT_PROTOCOL_VERSION] and forward-migrates
 * older versions lazily on read. Distinct from the crypto-format version owned by the encryption layer.
 */
const val CURRENT_PROTOCOL_VERSION: Int = 1

/** version of the flat Drive namespace layout (filenames + sidecars) */
const val CURRENT_LAYOUT_VERSION: Int = 1

/** plaintext meta-file at the root of the app-data folder */
@Serializable
data class SyncProtocolMetadata(
    val protocolVersion: Int = CURRENT_PROTOCOL_VERSION,
    val layoutVersion: Int = CURRENT_LAYOUT_VERSION,
    val createdAtUtcMillis: Long,
)

/** plaintext sidecar for cheap snapshot selection without decrypting candidates */
@Serializable
data class SyncSnapshotIndex(
    val snapshotId: String,
    val createdByDevice: String,
    val createdAtUtcMillis: Long,
    val protocolVersion: Int = CURRENT_PROTOCOL_VERSION,
)
