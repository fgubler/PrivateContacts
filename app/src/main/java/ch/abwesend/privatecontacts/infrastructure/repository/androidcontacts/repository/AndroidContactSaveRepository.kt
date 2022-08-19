package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository

import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactSaveRepository

class AndroidContactSaveRepository : AndroidContactRepositoryBase(), IAndroidContactSaveRepository {
    override suspend fun deleteContacts(contactIds: List<IContactIdExternal>): ContactDeleteResult {
        TODO("Implement")
    }
}
