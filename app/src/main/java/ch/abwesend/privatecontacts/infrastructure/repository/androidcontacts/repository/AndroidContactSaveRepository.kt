package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository

import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.NOT_YET_IMPLEMENTED_FOR_EXTERNAL_CONTACTS
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import com.alexstyl.contactstore.MutableContact

class AndroidContactSaveRepository : AndroidContactRepositoryBase() {
    suspend fun deleteContacts(contactIds: List<IContactIdExternal>) {
        checkContactWritePermission { exception -> throw exception }
        withContactStore { contactStore ->
            contactStore.execute { contactIds.forEach { delete(it.contactNo) } }
        }
    }
    suspend fun updateContact(contact: MutableContact) {
        checkContactWritePermission { exception -> throw exception }
        withContactStore { contactStore ->
            contactStore.execute { update(contact) }
        }
    }

    // TODO implement
    suspend fun createContact(contact: MutableContact): ContactSaveResult {
        checkContactWritePermission { exception -> throw exception }
        return ContactSaveResult.Failure(NOT_YET_IMPLEMENTED_FOR_EXTERNAL_CONTACTS)
    }
}
