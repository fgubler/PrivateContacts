/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.util

/**
 * Just a small helper for user-actions.
 * Do not use for anything security-relevant...
 */
fun addProtocolToUriString(uri: String, fallbackProtocol: String = "http"): String {
    val regex = Regex("^[a-zA-Z]+://")
    val containsProtocol = regex.containsMatchIn(uri)

    return if (containsProtocol) uri
    else "$fallbackProtocol://$uri"
}
