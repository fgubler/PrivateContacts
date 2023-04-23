/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain.model.contact

enum class ContactCategory {
    PERSON, // must not be renamed because the it is used as default-value in the database
    ORGANIZATION;

    companion object {
        const val nameOfPersonValue = "PERSON"
    }
}
