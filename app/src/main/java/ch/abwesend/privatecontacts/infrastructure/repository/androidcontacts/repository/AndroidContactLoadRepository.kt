/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository

import ch.abwesend.privatecontacts.domain.lib.flow.ErrorResource
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.service.valid
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.mapping.AndroidContactMapper
import com.alexstyl.contactstore.Contact
import com.alexstyl.contactstore.ContactGroup
import com.alexstyl.contactstore.ContactPredicate
import com.alexstyl.contactstore.ContactPredicate.ContactIdLookup
import com.alexstyl.contactstore.DisplayNameStyle.Alternative
import com.alexstyl.contactstore.DisplayNameStyle.Primary
import com.alexstyl.contactstore.GroupsPredicate
import com.alexstyl.contactstore.allContactColumns
import com.alexstyl.contactstore.coroutines.asFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * Repository to access the android ContactsProvider
 * Beware: Every public method must check for the permission
 */
class AndroidContactLoadRepository : AndroidContactRepositoryBase() {
    private val contactFactory: AndroidContactMapper by injectAnywhere()

    suspend fun resolveContactRaw(contactId: IContactIdExternal): Contact {
        logger.debug("Resolving contact for id $contactId")
        checkContactReadPermission { exception -> throw exception }

        return withContactStore { contactStore ->
            contactStore.fetchContacts(
                predicate = ContactIdLookup(contactId = contactId.contactNo),
                columnsToFetch = allContactColumns()
            ).asFlow()
                .flowOn(dispatchers.io)
                .firstOrNull()
                .also { logger.debug("Found ${it?.size} contacts matching $contactId") }
                ?.firstOrNull()
        } ?: resolveContactRawByLookupKey(contactId)
    }

    /**
     * For some reason, the contactNo can change sometimes, e.g. when updating a contact.
     * => use the LookupKey as fallback.
     */
    private suspend fun resolveContactRawByLookupKey(contactId: IContactIdExternal): Contact {
        logger.info("Resolving contact for id $contactId by lookup-key as fallback.")
        val newContactId = createContactsBaseFlow()
            .firstOrNull()
            .orEmpty()
            .map { it.id }
            .filterIsInstance<IContactIdExternal>()
            .singleOrNull { !it.lookupKey.isNullOrEmpty() && it.lookupKey == contactId.lookupKey } // enforce a unique match
            ?: throw IllegalArgumentException("Contact $contactId not found on android")
        return resolveContactRaw(newContactId)
    }

    suspend fun loadAllFullContactsRaw(): List<Contact> {
        logger.debug("Loading all full contacts")
        checkContactReadPermission { exception -> throw exception }

        return withContactStore { contactStore ->
            contactStore.fetchContacts(columnsToFetch = allContactColumns()).asFlow()
                .flowOn(dispatchers.io)
                .firstOrNull()
                .also { logger.debug("Found ${it?.size} contacts") }
        }.orEmpty()
    }

    suspend fun loadContactsSnapshot(
        predicate: ContactPredicate? = null
    ): List<IContactBase> = withContactStore { contactStore ->
        checkContactReadPermission { exception -> throw exception }

        contactStore.fetchContacts(predicate = predicate)
            .asFlow()
            .flowOn(dispatchers.io)
            .firstOrNull()
            .orEmpty()
            .mapNotNull { contactFactory.toContactBase(contact = it, rethrowExceptions = false) }
    }

    suspend fun createContactsBaseFlow(
        predicate: ContactPredicate? = null
    ): Flow<List<IContactBase>> {
        checkContactReadPermission { exception ->
            return flow {
                ErrorResource<List<IContactBase>>(listOf(exception))
            }
        }

        val displayNameStyle = if (Settings.current.orderByFirstName) Primary else Alternative
        val androidContacts = withContactStore { contactStore ->
            contactStore.fetchContacts(
                predicate = predicate,
                displayNameStyle = displayNameStyle,
            ).asFlow()
        }

        val contacts = androidContacts.map { contacts ->
            logger.debug("Loaded ${contacts.size} android contacts with predicate $predicate")
            contacts.mapNotNull { contactFactory.toContactBase(contact = it, rethrowExceptions = false) }
                .filter { validationService.validateContactBase(it).valid }
        }.flowOn(dispatchers.io)

        return contacts
    }

    suspend fun loadAllContactGroups(): List<ContactGroup> = loadContactGroupsByPredicate(predicate = null)

    suspend fun loadContactGroups(contact: Contact?): List<ContactGroup> {
        logger.debug("Resolving contact groups for contact ${contact?.contactId}")
        val contactGroupIds = contact?.groups.orEmpty().map { it.groupId }
        return loadContactGroupsByIds(contactGroupIds)
            .also { logger.debug("Found ${it.size} contact-groups for contact ${contact?.contactId}") }
    }

    suspend fun loadContactGroupsByIds(contactGroupIds: List<Long>): List<ContactGroup> {
        logger.debug("Resolving contact groups for ${contactGroupIds.size}")
        return if (contactGroupIds.isEmpty()) emptyList()
        else {
            checkContactReadPermission { exception -> throw exception }
            loadContactGroupsByPredicate(GroupsPredicate.GroupLookup(contactGroupIds))
        }
    }

    private suspend fun loadContactGroupsByPredicate(predicate: GroupsPredicate?): List<ContactGroup> {
        logger.debug("Resolving contact groups by predicate")
        checkContactReadPermission { exception -> throw exception }

        return withContactStore { contactStore ->
            contactStore.fetchContactGroups(predicate)
                .asFlow()
                .flowOn(dispatchers.io)
                .firstOrNull()
                .orEmpty()
                .also { logger.debug("Found ${it.size} contact-groups for predicated $predicate") }
        }
    }
}
