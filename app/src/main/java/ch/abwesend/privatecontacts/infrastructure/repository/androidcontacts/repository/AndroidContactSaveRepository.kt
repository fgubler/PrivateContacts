package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository

import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import com.alexstyl.contactstore.InternetAccount
import com.alexstyl.contactstore.MutableContact

class AndroidContactSaveRepository : AndroidContactRepositoryBase() {
    suspend fun deleteContacts(contactIds: Set<IContactIdExternal>) {
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

    /** [saveInAccount] if null is passed, the account is stored locally */
    suspend fun createContact(contact: MutableContact, saveInAccount: InternetAccount?) {
        checkContactWritePermission { exception -> throw exception }
        withContactStore { contactStore ->
            contactStore.execute {
                insert(contact, saveInAccount)
            }
        }
    }
}
