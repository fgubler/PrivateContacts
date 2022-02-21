/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.search

sealed interface ContactSearchConfig {
    object All : ContactSearchConfig
    data class Query(val query: String) : ContactSearchConfig
}
