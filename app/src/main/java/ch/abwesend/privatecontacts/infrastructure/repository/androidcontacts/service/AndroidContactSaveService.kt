package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.service

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.ModelStatus.CHANGED
import ch.abwesend.privatecontacts.domain.model.ModelStatus.NEW
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_CREATE_CONTACT_GROUP
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_DELETE_CONTACT
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_SAVE_CONTACT
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.batch.ContactBatchChangeErrors
import ch.abwesend.privatecontacts.domain.model.result.batch.ContactBatchChangeResult
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactSaveService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory.toInternetAccountOrNull
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory.toNewAndroidContactGroup
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository.AndroidContactLoadRepository
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository.AndroidContactSaveRepository
import com.alexstyl.contactstore.MutableContact
import com.alexstyl.contactstore.mutableCopy

class AndroidContactSaveService : IAndroidContactSaveService {
    private val contactSaveRepository: AndroidContactSaveRepository by injectAnywhere()
    private val contactLoadRepository: AndroidContactLoadRepository by injectAnywhere()
    private val contactLoadService: AndroidContactLoadService by injectAnywhere()
    private val contactChangeService: AndroidContactChangeService by injectAnywhere()

    override suspend fun deleteContacts(contactIds: Collection<IContactIdExternal>): ContactBatchChangeResult {
        val toDelete = contactIds.toSet()
        return try {
            contactSaveRepository.deleteContacts(toDelete)
            checkForSuccessfulDeletion(toDelete)
        } catch (t: Throwable) {
            logger.error("Failed to delete contacts $contactIds", t)
            checkForPartialDeletionSuccess(toDelete)
        }
    }

    private suspend fun checkForSuccessfulDeletion(contactIds: Set<IContactIdExternal>): ContactBatchChangeResult {
        val existenceByContactId = contactLoadService.doContactsExist(contactIds)
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

    private suspend fun checkForPartialDeletionSuccess(contactIds: Set<IContactIdExternal>): ContactBatchChangeResult {
        val deletedContacts: Set<ContactId> = try {
            val doContactsExist = contactLoadService.doContactsExist(contactIds)
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
            // TODO when a contact is updated, "saveInAccount" is always "NONE" => does not yet work properly
            val contactGroupResult = createMissingContactGroups(contact.saveInAccount, contact.contactGroups)
            val existingGroups = contactLoadService.getContactGroups(contact.saveInAccount) // including the new ones

            val contactToChange = originalContactRaw.mutableCopy {
                contactChangeService.updateChangedBaseData(
                    originalContact = originalContact,
                    changedContact = contact,
                    mutableContact = this,
                )
                contactChangeService.updateChangedImage(changedContact = contact, mutableContact = this)
                contactChangeService.updateChangedContactData(changedContact = contact, mutableContact = this)
                contactChangeService.updateContactGroups(
                    changedContact = contact,
                    mutableContact = this,
                    allContactGroups = existingGroups,
                )
            }

            contactSaveRepository.updateContact(contactToChange)
            contactGroupResult // save the rest of the contact if the contact-groups fail but at least notify the user
        } catch (e: Exception) {
            logger.error("Failed to change contact $contactId", e)
            ContactSaveResult.Failure(UNABLE_TO_SAVE_CONTACT)
        }
    }

    override suspend fun createContact(contact: IContact): ContactSaveResult {
        return try {
            val contactGroupResult = createMissingContactGroups(contact.saveInAccount, contact.contactGroups)
            val existingGroups = contactLoadService.getContactGroups(contact.saveInAccount) // including the new ones

            val mutableContact = contact.toAndroidContact(existingGroups)
            val account = contact.saveInAccount.toInternetAccountOrNull()

            contactSaveRepository.createContact(mutableContact, account)
            contactGroupResult // save the rest of the contact if the contact-groups fail but at least notify the user
        } catch (e: Exception) {
            logger.error("Failed to create contact ${contact.id}", e)
            ContactSaveResult.Failure(UNABLE_TO_SAVE_CONTACT)
        }
    }

    override suspend fun createMissingContactGroups(
        account: ContactAccount,
        groups: List<ContactGroup>,
    ): ContactSaveResult =
        try {
            val existingGroups = contactLoadService.getContactGroups(account)
            val groupsToCreate = filterForContactGroupsToCreate(groups = groups, existingGroups = existingGroups)
            val transformedGroups = groupsToCreate.map { it.toNewAndroidContactGroup(account) }
            contactSaveRepository.createContactGroups(transformedGroups)
            ContactSaveResult.Success
        } catch (e: Exception) {
            logger.error("Failed to create contact groups", e)
            ContactSaveResult.Failure(UNABLE_TO_CREATE_CONTACT_GROUP)
        }

    private fun filterForContactGroupsToCreate(
        groups: List<ContactGroup>,
        existingGroups: List<ContactGroup>
    ): List<ContactGroup> {
        val changedGroups = groups.filter { it.modelStatus in listOf(NEW, CHANGED) }

        val matchingGroupsById = changedGroups.filter { changedGroup ->
            existingGroups.any { existingGroup -> existingGroup.id.groupNo == changedGroup.id.groupNo }
        }.toSet()
        val remainingGroups = changedGroups.minus(matchingGroupsById)
        val matchingGroupsByName = remainingGroups.filter { changedGroup ->
            existingGroups.any { existingGroup -> existingGroup.id.name == changedGroup.id.name }
        }.toSet()

        val unmatchedGroups = remainingGroups.minus(matchingGroupsByName)
        logger.debug("Found ${unmatchedGroups.size} contact groups to create")
        return unmatchedGroups
    }

    private fun IContact.toAndroidContact(allContactGroups: List<ContactGroup>): MutableContact {
        val mutableContact = MutableContact()
        contactChangeService.updateChangedBaseData(
            originalContact = null,
            changedContact = this,
            mutableContact = mutableContact,
        )
        contactChangeService.updateChangedImage(changedContact = this, mutableContact = mutableContact)
        contactChangeService.updateChangedContactData(changedContact = this, mutableContact = mutableContact)
        contactChangeService.updateContactGroups(
            changedContact = this,
            mutableContact = mutableContact,
            allContactGroups = allContactGroups,
        )
        return mutableContact
    }
}
