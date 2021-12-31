package ch.abwesend.privatecontacts.domain.repository

import ch.abwesend.privatecontacts.domain.model.contact.Contact
import ch.abwesend.privatecontacts.domain.model.contact.ContactBase
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult

interface IContactRepository {
    suspend fun loadContacts(): List<ContactBase>
    suspend fun resolveContact(contact: ContactBase): Contact
    suspend fun createContact(contact: Contact): ContactSaveResult
    suspend fun updateContact(contact: Contact): ContactSaveResult
}
