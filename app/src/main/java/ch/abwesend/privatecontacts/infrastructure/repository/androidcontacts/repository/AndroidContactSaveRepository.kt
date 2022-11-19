package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_DELETE_CONTACT
import ch.abwesend.privatecontacts.domain.model.result.batch.ContactBatchChangeErrors
import ch.abwesend.privatecontacts.domain.model.result.batch.ContactBatchChangeResult
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactSaveRepository
import ch.abwesend.privatecontacts.domain.util.injectAnywhere

class AndroidContactSaveRepository : AndroidContactRepositoryBase(), IAndroidContactSaveRepository {
    private val contactLoadRepository: AndroidContactLoadRepository by injectAnywhere()

    override suspend fun deleteContacts(contactIds: List<IContactIdExternal>): ContactBatchChangeResult =
        try {
            checkContactWritePermission { exception -> throw exception }

            withContactStore { contactStore ->
                contactStore.execute { contactIds.forEach { delete(it.contactNo) } }
            }

            val existenceByContactId = contactLoadRepository.doContactsExist(contactIds.toSet())
            val deletedContacts = existenceByContactId.filter { !it.value }.keys.toList()
            val notDeletedContacts: Map<ContactId, ContactBatchChangeErrors> = existenceByContactId
                .filter { it.value }.keys
                .associateWith {
                    ContactBatchChangeErrors(
                        errors = listOf(UNABLE_TO_DELETE_CONTACT),
                        validationErrors = emptyList(),
                    )
                }

            ContactBatchChangeResult(successfulChanges = deletedContacts, failedChanges = notDeletedContacts)
        } catch (t: Throwable) {
            logger.error("Failed to delete contacts $contactIds", t)
            checkForPartialSuccess(contactIds)
        }

    private suspend fun checkForPartialSuccess(contactIds: List<IContactIdExternal>): ContactBatchChangeResult {
        val deletedContacts: Set<ContactId> = try {
            val doContactsExist = contactLoadRepository.doContactsExist(contactIds.toSet())
            doContactsExist.filter { !it.value }.keys
        } catch (t: Throwable) {
            logger.error("Failed to check if some contacts were deleted successfully", t)
            emptySet()
        }
        val notDeletedContacts = contactIds
            .minus(deletedContacts)
            .associateWith {
                ContactBatchChangeErrors(
                    errors = listOf(UNABLE_TO_DELETE_CONTACT),
                    validationErrors = emptyList(),
                )
            }
        return ContactBatchChangeResult(
            successfulChanges = deletedContacts.toList(),
            failedChanges = notDeletedContacts,
        )
    }
}
