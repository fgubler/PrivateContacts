/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ezvcard.property.Uid
import java.util.UUID

private const val UID_PREFIX = "urn:uuid:"
const val PROTON_WEB_PREFIX = "proton-web-"
const val PROTON_AUTOSAVE_PREFIX = "proton-autosave-"

fun UUID.toUid(): Uid = Uid(UID_PREFIX + toString())
fun Uid.toUuidOrNull(): UUID? {
    val uidValue = value.orEmpty()
        .replaceFirst(oldValue = UID_PREFIX, newValue = "")
        .replaceFirst(oldValue = PROTON_WEB_PREFIX, newValue = "")
        .replaceFirst(oldValue = PROTON_AUTOSAVE_PREFIX, newValue = "")

    val uuid = runCatching { UUID.fromString(uidValue) }

    return uuid.getOrElse {
        logger.warning("Invalid UID '$uidValue': cannot parse to UUID", it)
        null
    }
}
