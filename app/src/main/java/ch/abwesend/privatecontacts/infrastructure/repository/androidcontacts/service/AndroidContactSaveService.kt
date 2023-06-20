package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.service

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.filterShouldUpsert
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_CREATE_CONTACT_GROUP
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_DELETE_CONTACT
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_RESOLVE_EXISTING_CONTACT
import ch.abwesend.privatecontacts.domain.model.result.ContactChangeError.UNABLE_TO_SAVE_CONTACT
import ch.abwesend.privatecontacts.domain.model.result.ContactSaveResult
import ch.abwesend.privatecontacts.domain.model.result.batch.ContactBatchChangeErrors
import ch.abwesend.privatecontacts.domain.model.result.batch.ContactIdBatchChangeResult
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactSaveService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.factory.IAndroidContactMutableFactory
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.mapping.toInternetAccountOrNull
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.mapping.toNewAndroidContactGroup
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.model.IAndroidContactMutable
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository.AndroidContactLoadRepository
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository.AndroidContactSaveRepository

class AndroidContactSaveService : IAndroidContactSaveService {
    private val contactSaveRepository: AndroidContactSaveRepository by injectAnywhere()
    private val contactLoadRepository: AndroidContactLoadRepository by injectAnywhere()
    private val contactLoadService: AndroidContactLoadService by injectAnywhere()
    private val contactChangeService: AndroidContactChangeService by injectAnywhere()
    private val contactAccountService: AndroidContactAccountService by injectAnywhere()
    private val contactMutableFactory: IAndroidContactMutableFactory by injectAnywhere()

    override suspend fun deleteContacts(contactIds: Collection<IContactIdExternal>): ContactIdBatchChangeResult {
        val toDelete = contactIds.toSet()
        return try {
            contactSaveRepository.deleteContacts(toDelete)
            checkForSuccessfulDeletion(toDelete)
        } catch (t: Throwable) {
            logger.error("Failed to delete contacts $contactIds", t)
            checkForPartialDeletionSuccess(toDelete)
        }
    }

    private suspend fun checkForSuccessfulDeletion(contactIds: Set<IContactIdExternal>): ContactIdBatchChangeResult {
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

        return ContactIdBatchChangeResult(successfulChanges = deletedContacts, failedChanges = notDeletedContacts)
    }

    private suspend fun checkForPartialDeletionSuccess(contactIds: Set<IContactIdExternal>): ContactIdBatchChangeResult {
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
        return ContactIdBatchChangeResult(
            successfulChanges = deletedContacts.toList(),
            failedChanges = notDeletedContacts,
        )
    }

    override suspend fun updateContact(contactId: IContactIdExternal, contact: IContact): ContactSaveResult {
        val originalContactRaw = try {
            contactLoadRepository.resolveContactRaw(contactId)
        } catch (e: IllegalArgumentException) {
            logger.error("Failed to load contact $contactId for updating it", e)
            return ContactSaveResult.Failure(UNABLE_TO_RESOLVE_EXISTING_CONTACT)
        }

        val originalContact = try {
            contactLoadService.resolveContact(contactId, originalContactRaw)
        } catch (e: IllegalStateException) {
            logger.error("Failed to resolve contact $contactId for updating it", e)
            return ContactSaveResult.Failure(UNABLE_TO_RESOLVE_EXISTING_CONTACT)
        }

        return try {
            val contactGroupResult = createMissingContactGroupsOnUpdate(contact)
            // don't know the correct account for sure => load all groups
            val existingGroups = contactLoadService.getAllContactGroups() // including the new ones
            val contactToChange = contactMutableFactory.toAndroidContactMutable(originalContactRaw)

            contactChangeService.updateChangedBaseData(
                originalContact = originalContact,
                changedContact = contact,
                mutableContact = contactToChange,
            )
            contactChangeService.updateChangedImage(changedContact = contact, mutableContact = contactToChange)
            contactChangeService.updateChangedContactData(changedContact = contact, mutableContact = contactToChange)

            contactChangeService.updateContactGroups(
                changedContact = contact,
                mutableContact = contactToChange,
                allContactGroups = existingGroups,
            )

            contactSaveRepository.updateContact(contactToChange)
            contactGroupResult // save the rest of the contact if the contact-groups fail but at least notify the user
        } catch (e: Exception) {
            logger.error("Failed to change contact $contactId", e)
            ContactSaveResult.Failure(UNABLE_TO_SAVE_CONTACT)
        }
    }

    private suspend fun createMissingContactGroupsOnUpdate(contact: IContact): ContactSaveResult {
        val changedGroups = contact.contactGroups.filterShouldUpsert()
        return if (changedGroups.isEmpty()) {
            logger.debug("No contact-groups were changed: nothing to create")
            ContactSaveResult.Success
        } else {
            val correspondingAccount = contactAccountService.getBestGuessForCorrespondingAccount(contact)
            createMissingContactGroups(correspondingAccount, contact.contactGroups)
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
        val changedGroups = groups.filterShouldUpsert()

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

    private fun IContact.toAndroidContact(allContactGroups: List<ContactGroup>): IAndroidContactMutable {
        val mutableContact = contactMutableFactory.create()
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
