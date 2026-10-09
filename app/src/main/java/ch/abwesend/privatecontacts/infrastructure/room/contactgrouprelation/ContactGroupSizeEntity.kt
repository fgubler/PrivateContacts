/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.contactgrouprelation

/** Result of an aggregation query: not a table of its own */
data class ContactGroupSizeEntity(
    val contactGroupName: String,
    val numberOfContacts: Int,
)
