/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.sync

import kotlinx.serialization.json.Json

/**
 * Shared JSON configuration for all sync payloads. [Json.ignoreUnknownKeys] enables additive,
 * forward-compatible schema evolution (older readers skip fields they do not know).
 */
val syncJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}
