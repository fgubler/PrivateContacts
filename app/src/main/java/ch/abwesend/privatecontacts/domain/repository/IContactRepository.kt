package ch.abwesend.privatecontacts.domain.repository

import ch.abwesend.privatecontacts.domain.model.contact.Contact
import ch.abwesend.privatecontacts.domain.model.contact.ContactBase

interface IContactRepository {
    suspend fun loadContacts(): List<ContactBase>
    suspend fun resolveContact(contact: ContactBase): Contact
    suspend fun createContact(): Contact
}
