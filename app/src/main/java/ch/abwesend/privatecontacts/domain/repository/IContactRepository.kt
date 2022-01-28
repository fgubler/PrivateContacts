package ch.abwesend.privatecontacts.domain.repository

import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult

interface IContactRepository {
    suspend fun loadContacts(): List<IContactBase>
    suspend fun getContactsPaged(loadSize: Int, offsetInRows: Int): List<IContactBase>

    suspend fun resolveContact(contact: IContactBase): IContact
    suspend fun createContact(contact: IContact): ContactSaveResult
    suspend fun updateContact(contact: IContact): ContactSaveResult
}
