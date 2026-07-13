/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.util

import java.util.UUID

/**
 * Deterministically derives a UUID from the given [parts].
 *
 * The byte-encoding is pinned so every device computes a byte-identical result: the parts are
 * lower-cased, joined with '|', encoded as UTF-8 with no trailing newline. Uses
 * [UUID.nameUUIDFromBytes] (version 3 / MD5); this is not security-sensitive, only a stable identity.
 */
fun deterministicUuid(vararg parts: String): UUID {
    val canonical = parts.joinToString(separator = "|") { it.lowercase() }
    val bytes = canonical.toByteArray(Charsets.UTF_8)
    return UUID.nameUUIDFromBytes(bytes)
}
