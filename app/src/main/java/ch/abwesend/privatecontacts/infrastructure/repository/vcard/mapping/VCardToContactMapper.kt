/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.vcard.mapping

import ch.abwesend.privatecontacts.domain.model.contact.ContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ezvcard.VCard

// TODO add unit tests
class VCardToContactMapper {
    fun mapToContact(vCard: VCard): IContact {
        val uuid = vCard.uid?.toUuidOrNull()
        val contactId = uuid?.let { ContactIdInternal(uuid = it) } ?: ContactIdInternal.randomId()
        val contact = ContactEditable.createNew(id = contactId)

        // TODO implement

        return contact
    }
}
