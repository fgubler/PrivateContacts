/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.domain

import ch.abwesend.privatecontacts.domain.model.contact.ContactType

// TODO replace this with something serious
object Settings {
    val orderByFirstName: Boolean = true
    val defaultContactType: ContactType = ContactType.PRIVATE
}
