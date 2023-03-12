package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository

import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.model.IAndroidContactMutable
import com.alexstyl.contactstore.InternetAccount
import com.alexstyl.contactstore.MutableContactGroup

class AndroidContactSaveRepository : AndroidContactRepositoryBase() {
    suspend fun deleteContacts(contactIds: Set<IContactIdExternal>) {
        if (contactIds.isEmpty()) {
            return
        }
        checkContactWritePermission { exception -> throw exception }

        withContactStore { contactStore ->
            contactStore.execute { contactIds.forEach { delete(it.contactNo) } }
        }
    }
    suspend fun updateContact(contact: IAndroidContactMutable) {
        val mutableContact = contact.toMutableContact()
        checkContactWritePermission { exception -> throw exception }
        withContactStore { contactStore ->
            contactStore.execute { update(mutableContact) }
        }
    }

    /** [saveInAccount] if null is passed, the account is stored locally */
    suspend fun createContact(contact: IAndroidContactMutable, saveInAccount: InternetAccount?) {
        val mutableContact = contact.toMutableContact()
        checkContactWritePermission { exception -> throw exception }
        withContactStore { contactStore ->
            contactStore.execute {
                insert(mutableContact, saveInAccount)
            }
        }
    }

    suspend fun createContactGroups(groups: List<MutableContactGroup>) {
        if (groups.isEmpty()) {
            return
        }
        checkContactWritePermission { exception -> throw exception }

        withContactStore { contactStore ->
            contactStore.execute {
                groups.forEach {
                    insertGroup(it)
                }
            }
        }
    }
}
