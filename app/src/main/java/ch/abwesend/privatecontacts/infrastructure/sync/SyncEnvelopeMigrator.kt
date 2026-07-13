/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.sync

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.sync.CURRENT_PROTOCOL_VERSION
import ch.abwesend.privatecontacts.domain.model.sync.SyncCommandEnvelope

/**
 * Lazily forward-migrates parsed sync payloads on read. Nothing on Drive is ever rewritten in place;
 * the next snapshot retires old-version data. A payload with a newer protocol version than this app
 * supports is refused (the user is asked to update).
 */
class SyncEnvelopeMigrator {
    /** @return the migrated envelope, or null if its protocol version is newer than supported */
    fun migrateOrNull(envelope: SyncCommandEnvelope): SyncCommandEnvelope? =
        when {
            envelope.protocolVersion > CURRENT_PROTOCOL_VERSION -> {
                logger.warning(
                    "Refusing sync command with protocol version ${envelope.protocolVersion} " +
                        "(supported: $CURRENT_PROTOCOL_VERSION) - please update the app"
                )
                null
            }
            else -> envelope // v1: no migration needed
        }

    fun isProtocolSupported(protocolVersion: Int): Boolean =
        protocolVersion <= CURRENT_PROTOCOL_VERSION
}
