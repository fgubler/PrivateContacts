/*
 * Private Contacts
 * Copyright (c) 2026.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.contactsandroid.repository

import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.logging.debugLocally
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.permission.MissingPermissionException
import ch.abwesend.privatecontacts.domain.service.interfaces.PermissionService
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.repository.contactsandroid.mapping.ContactsAndroidContactMapper
import ch.abwesend.privatecontacts.infrastructure.repository.contactsandroid.mapping.toContactGroup
import contacts.core.Contacts
import contacts.core.Fields
import contacts.core.entities.Contact
import contacts.core.entities.Group
import contacts.core.equalTo
import contacts.core.util.lookupKeyIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class ContactsAndroidLoadRepository {
    private val contactsApi: Contacts by injectAnywhere()
    private val permissionService: PermissionService by injectAnywhere()
    private val dispatchers: IDispatchers by injectAnywhere()
    private val contactMapper: ContactsAndroidContactMapper by injectAnywhere()

    private val hasContactReadPermission: Boolean
        get() = permissionService.hasContactReadPermission()

    suspend fun resolveContactRaw(contactId: IContactIdExternal): Contact {
        logger.debug("Resolving contact for id $contactId via contacts-android")
        checkReadPermission()

        return withContext(dispatchers.io) {
            contactsApi.query()
                .where { Fields.Contact.Id equalTo contactId.contactNo }
                .find()
                .firstOrNull()
        } ?: resolveContactRawByLookupKey(contactId)
    }

    private suspend fun resolveContactRawByLookupKey(contactId: IContactIdExternal): Contact {
        logger.info("Resolving contact for id $contactId by lookup-key as fallback (contacts-android).")
        val lookupKey = contactId.lookupKey
            ?: throw IllegalArgumentException("Contact $contactId not found and has no lookup-key")

        return withContext(dispatchers.io) {
            contactsApi.query()
                .where { Fields.Contact.LookupKey equalTo lookupKey }
                .find()
                .firstOrNull()
        } ?: throw IllegalArgumentException("Contact $contactId not found on android (contacts-android)")
    }

    suspend fun loadAllFullContactsRaw(): List<Contact> {
        logger.debug("Loading all full contacts via contacts-android")
        checkReadPermission()

        return withContext(dispatchers.io) {
            contactsApi.query().find()
        }
    }

    suspend fun findContactsWithPhoneNumber(phoneNumber: String): List<Contact> {
        logger.debugLocally("Loading all contacts with phone-number $phoneNumber via contacts-android")
        checkReadPermission()

        return withContext(dispatchers.io) {
            contactsApi.phoneLookupQuery()
                .whereExactlyMatches(phoneNumber)
                .find()
        }
    }

    suspend fun loadContactsSnapshot(): List<IContactBase> {
        checkReadPermission()

        return withContext(dispatchers.io) {
            contactsApi.query()
                .include(Fields.Contact.Id, Fields.Contact.LookupKey, Fields.Contact.DisplayNamePrimary)
                .find()
                .mapNotNull { contactMapper.toContactBase(contact = it, rethrowExceptions = false) }
        }
    }

    fun createContactsBaseFlow(searchQuery: String? = null): Flow<List<IContactBase>> = flow {
        checkReadPermission()

        val contacts = withContext(dispatchers.io) {
            if (searchQuery != null) {
                contactsApi.broadQuery()
                    .wherePartiallyMatches(searchQuery)
                    .find()
            } else {
                contactsApi.query()
                    .include(Fields.Contact.Id, Fields.Contact.LookupKey, Fields.Contact.DisplayNamePrimary)
                    .find()
            }
        }

        logger.debug("Loaded ${contacts.size} contacts via contacts-android (query=$searchQuery)")
        val mapped = contacts.mapNotNull { contactMapper.toContactBase(contact = it, rethrowExceptions = false) }
        emit(mapped)
    }.flowOn(dispatchers.io)

    suspend fun loadAllContactGroups(): List<Group> {
        checkReadPermission()

        return withContext(dispatchers.io) {
            contactsApi.groups().query().find()
        }
    }

    suspend fun loadContactGroups(contact: Contact?): List<ContactGroup> {
        logger.debug("Resolving contact groups for contact ${contact?.id} via contacts-android")
        val groupMembershipIds = contact?.rawContacts
            ?.flatMap { it.groupMemberships }
            ?.mapNotNull { it.groupId }
            .orEmpty()

        if (groupMembershipIds.isEmpty()) return emptyList()

        val allGroups = loadAllContactGroups()
        return allGroups
            .filter { it.id in groupMembershipIds }
            .map { it.toContactGroup() }
            .also { logger.debug("Found ${it.size} contact-groups for contact ${contact?.id}") }
    }

    private fun checkReadPermission() {
        if (!hasContactReadPermission) {
            val errorMessage = "Trying to read android contacts without read-permission (contacts-android)."
                .also { logger.warning(it) }
            throw MissingPermissionException(errorMessage)
        }
    }
}
