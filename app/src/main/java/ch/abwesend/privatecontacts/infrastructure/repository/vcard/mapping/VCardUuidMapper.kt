/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ezvcard.property.Uid
import java.util.UUID

// TODO add unit tests
private const val UID_PREFIX = "urn:uuid:"
fun UUID.toUid(): Uid = Uid(UID_PREFIX + toString())
fun Uid.toUuidOrNull(): UUID? = runCatching { UUID.fromString(value) }.getOrElse {
    logger.warning("Invalid UID '${this.value}': cannot parse to UUID", it)
    null
}
