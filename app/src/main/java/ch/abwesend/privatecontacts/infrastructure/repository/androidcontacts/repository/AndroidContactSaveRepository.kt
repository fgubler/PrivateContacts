package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.NOT_YET_IMPLEMENTED_FOR_EXTERNAL_CONTACTS
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_DELETE_CONTACT
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.batch.ContactBatchChangeErrors
import ch.abwesend.privatecontacts.domain.model.result.batch.ContactBatchChangeResult
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactSaveRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

class AndroidContactSaveRepository : AndroidContactRepositoryBase(), IAndroidContactSaveRepository {
    private val contactLoadRepository: AndroidContactLoadRepository by injectAnywhere()

    override suspend fun deleteContacts(contactIds: List<IContactIdExternal>): ContactBatchChangeResult =
        try {
            checkContactWritePermission { exception -> throw exception }

            val deletedContacts: List<ContactId> = withContactStore { contactStore ->
                contactStore.execute {
                    contactIds.forEach { delete(it.contactNo) }
                }
                contactIds.filter { !contactLoadRepository.doesContactExist(it) }
            }
            val notDeletedContacts = contactIds
                .minus(deletedContacts.toSet())
                .associateWith {
                    ContactBatchChangeErrors(
                        errors = listOf(UNABLE_TO_DELETE_CONTACT),
                        validationErrors = emptyList(),
                    )
                }

            ContactBatchChangeResult(successfulChanges = deletedContacts, failedChanges = notDeletedContacts)
        } catch (t: Throwable) {
            logger.error("Failed to delete contacts $contactIds", t)
            checkForPartialDeletionSuccess(contactIds)
        }

    private suspend fun checkForPartialDeletionSuccess(contactIds: List<IContactIdExternal>): ContactBatchChangeResult {
        val deletedContacts = mutableListOf<ContactId>() // use the mutable list for partial results in case of failure
        try {
            contactIds.forEach {
                if (!contactLoadRepository.doesContactExist(it)) {
                    deletedContacts.add(it)
                }
            }
        } catch (t: Throwable) {
            logger.error("Failed to check if some contacts were deleted successfully", t)
        }
        val notDeletedContacts = contactIds
            .minus(deletedContacts.toSet())
            .associateWith {
                ContactBatchChangeErrors(
                    errors = listOf(UNABLE_TO_DELETE_CONTACT),
                    validationErrors = emptyList(),
                )
            }
        return ContactBatchChangeResult(successfulChanges = deletedContacts, failedChanges = notDeletedContacts)
    }

    override suspend fun updateContact(contactId: IContactIdExternal, contact: IContact): ContactSaveResult {
        val originalContactRaw = contactLoadRepository.resolveContactRaw(contactId)
        val originalContact = contactLoadRepository.resolveContact(contactId, originalContactRaw)

        withContactStore { contactStore ->
            contactStore.execute {
            }
        }
    }

    // TODO implement
    override suspend fun createContact(contactId: IContactIdInternal, contact: IContact): ContactSaveResult {
        return ContactSaveResult.Failure(NOT_YET_IMPLEMENTED_FOR_EXTERNAL_CONTACTS)
    }
}
