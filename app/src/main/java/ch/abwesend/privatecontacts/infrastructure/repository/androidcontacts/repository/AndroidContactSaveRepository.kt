package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.result.ContactBatchChangeResult
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactLoadRepository
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactSaveRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

class AndroidContactSaveRepository : AndroidContactRepositoryBase(), IAndroidContactSaveRepository {
    private val contactLoadRepository: IAndroidContactLoadRepository by injectAnywhere()

    override suspend fun deleteContacts(contactIds: List<IContactIdExternal>): ContactBatchChangeResult =
        try {
            checkContactWritePermission { exception -> throw exception }

            val deletedContacts: List<ContactId> = withContactStore { contactStore ->
                contactStore.execute {
                    contactIds.forEach { delete(it.contactNo) }
                }
                contactIds.filter { !contactLoadRepository.doesContactExist(it) }
            }
            val notDeletedContacts = contactIds.minus(deletedContacts.toSet())

            ContactBatchChangeResult(successfulChanges = deletedContacts, failedChanges = notDeletedContacts)
        } catch (t: Throwable) {
            logger.error("Failed to delete contacts $contactIds", t)
            // TODO check if any of the contacts was actually successful? Stop at first error
            ContactBatchChangeResult.failure(contactIds)
        }
}
