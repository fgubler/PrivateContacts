/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contact

/**
 * Account in which a contact can be stored.
 * Only relevant for new, external contacts
 */
data class ContactAccount(val username: String, val accountProvider: String)
