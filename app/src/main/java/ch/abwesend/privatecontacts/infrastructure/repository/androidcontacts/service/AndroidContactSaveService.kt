package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.service

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.NOT_YET_IMPLEMENTED_FOR_EXTERNAL_CONTACTS
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_DELETE_CONTACT
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_SAVE_CONTACT
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.batch.ContactBatchChangeErrors
import ch.abwesend.privatecontacts.domain.model.result.batch.ContactBatchChangeResult
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactSaveService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository.AndroidContactLoadRepository
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository.AndroidContactSaveRepository
import com.alexstyl.contactstore.mutableCopy

class AndroidContactSaveService : IAndroidContactSaveService {
    private val contactSaveRepository: AndroidContactSaveRepository by injectAnywhere()
    private val contactLoadRepository: AndroidContactLoadRepository by injectAnywhere()
    private val contactLoadService: AndroidContactLoadService by injectAnywhere()
    private val changesApplicationService: AndroidContactChangesApplicationService by injectAnywhere()

    override suspend fun deleteContacts(contactIds: List<IContactIdExternal>): ContactBatchChangeResult =
        try {
            contactSaveRepository.deleteContacts(contactIds)
            checkForSuccessfulDeletion(contactIds)
        } catch (t: Throwable) {
            logger.error("Failed to delete contacts $contactIds", t)
            checkForPartialDeletionSuccess(contactIds)
        }

    private suspend fun checkForSuccessfulDeletion(contactIds: List<IContactIdExternal>): ContactBatchChangeResult {
        val existenceByContactId = contactLoadService.doContactsExist(contactIds.toSet())
        val deletedContacts = existenceByContactId.filter { !it.value }.keys.toList()
        val notDeletedContacts: Map<ContactId, ContactBatchChangeErrors> = existenceByContactId
            .filter { it.value }.keys
            .associateWith {
                ContactBatchChangeErrors(
                    errors = listOf(UNABLE_TO_DELETE_CONTACT),
                    validationErrors = emptyList(),
                )
            }

        return ContactBatchChangeResult(successfulChanges = deletedContacts, failedChanges = notDeletedContacts)
    }

    private suspend fun checkForPartialDeletionSuccess(contactIds: List<IContactIdExternal>): ContactBatchChangeResult {
        val deletedContacts: Set<ContactId> = try {
            val doContactsExist = contactLoadService.doContactsExist(contactIds.toSet())
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

    override suspend fun updateContact(contactId: IContactIdExternal, contact: IContact): ContactSaveResult {
        val originalContactRaw = contactLoadRepository.resolveContactRaw(contactId)
        val originalContact = contactLoadService.resolveContact(contactId, originalContactRaw)
        return try {
            val contactToChange = originalContactRaw.mutableCopy {
                changesApplicationService.updateChangedBaseData(
                    originalContact = originalContact,
                    changedContact = contact,
                    mutableContact = this,
                )
                changesApplicationService.updateChangedImage(changedContact = contact, mutableContact = this)
                changesApplicationService.updateChangedContactData(changedContact = contact, mutableContact = this)
            }

            // TODO update contact-groups on contact(per se and on the contact)
            contactSaveRepository.updateContact(contactToChange)

            ContactSaveResult.Success
        } catch (e: Exception) {
            logger.error("Failed to change contact $contactId", e)
            ContactSaveResult.Failure(UNABLE_TO_SAVE_CONTACT)
        }
    }

    // TODO implement
    override suspend fun createContact(contactId: IContactIdInternal, contact: IContact): ContactSaveResult {
        return ContactSaveResult.Failure(NOT_YET_IMPLEMENTED_FOR_EXTERNAL_CONTACTS)
    }
}
