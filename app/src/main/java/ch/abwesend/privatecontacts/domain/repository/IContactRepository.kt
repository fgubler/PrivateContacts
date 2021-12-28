package ch.abwesend.privatecontacts.domain.repository

import ch.abwesend.privatecontacts.domain.model.ContactBase

interface IContactRepository {
    suspend fun loadContacts(): List<ContactBase>
}
