package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNKNOWN_ERROR
import ch.abwesend.privatecontacts.domain.model.result.ContactDeleteResult
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactSaveRepository

// TODO consider whether to really catch exceptions here
class AndroidContactSaveRepository : AndroidContactRepositoryBase(), IAndroidContactSaveRepository {
    override suspend fun deleteContacts(contactIds: List<IContactIdExternal>): ContactDeleteResult =
        try {
            checkContactWritePermission { exception -> throw exception }

            withContactStore { contactStore ->
                contactStore.execute {
                    contactIds.forEach { delete(it.contactNo) }
                }
            }

            ContactDeleteResult.Success
        } catch (t: Throwable) {
            logger.error("Failed to delete contacts $contactIds", t)
            ContactDeleteResult.Failure(UNKNOWN_ERROR)
        }
}
