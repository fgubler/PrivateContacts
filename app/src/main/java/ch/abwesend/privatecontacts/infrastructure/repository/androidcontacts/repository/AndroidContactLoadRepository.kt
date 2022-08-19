/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository

import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.flow.ErrorResource
import ch.abwesend.privatecontacts.domain.lib.flow.ResourceFlow
import ch.abwesend.privatecontacts.domain.lib.flow.toResourceFlow
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.permission.MissingPermissionException
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig.All
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig.Query
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactLoadRepository
import ch.abwesend.privatecontacts.domain.service.ContactValidationService
import ch.abwesend.privatecontacts.domain.service.interfaces.PermissionService
import ch.abwesend.privatecontacts.domain.service.valid
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.toContact
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.toContactBase
import com.alexstyl.contactstore.Contact
import com.alexstyl.contactstore.ContactGroup
import com.alexstyl.contactstore.ContactPredicate
import com.alexstyl.contactstore.ContactPredicate.ContactLookup
import com.alexstyl.contactstore.ContactStore
import com.alexstyl.contactstore.DisplayNameStyle.Alternative
import com.alexstyl.contactstore.DisplayNameStyle.Primary
import com.alexstyl.contactstore.GroupsPredicate
import com.alexstyl.contactstore.allContactColumns
import com.alexstyl.contactstore.coroutines.asFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlin.system.measureTimeMillis

/**
 * Repository to access the android ContactsProvider
 */
class AndroidContactLoadRepository : IAndroidContactLoadRepository {
    private val permissionService: PermissionService by injectAnywhere()
    private val validationService: ContactValidationService by injectAnywhere()
    private val dispatchers: IDispatchers by injectAnywhere()

    private val contactStore: ContactStore by injectAnywhere()

    override fun loadContactsAsFlow(searchConfig: ContactSearchConfig): ResourceFlow<List<IContactBase>> =
        when (searchConfig) {
            is All -> loadContacts()
            is Query -> searchContacts(searchConfig.query)
        }

    override suspend fun resolveContact(contactId: IContactIdExternal): IContact {
        logger.debug("Resolving contact for id $contactId")
        checkContactReadPermission { exception -> throw exception }

        val contactRaw = loadFullContact(contactId.contactNo)
        val contactGroups = loadContactGroups(contactRaw)

        return contactRaw?.toContact(groups = contactGroups, rethrowExceptions = true)
            ?: throw IllegalArgumentException("Contact $contactId not found on android")
    }

    private suspend fun loadFullContact(contactNo: Long): Contact? =
        contactStore.fetchContacts(
            predicate = ContactLookup(contactId = contactNo),
            columnsToFetch = allContactColumns()
        ).asFlow()
            .flowOn(dispatchers.io)
            .firstOrNull()
            .also { logger.debug("Found ${it?.size} contacts matching $contactNo") }
            ?.firstOrNull()

    private suspend fun loadContactGroups(contact: Contact?): List<ContactGroup> {
        val contactGroupIds = contact?.groups.orEmpty().map { it.groupId }
        return if (contactGroupIds.isEmpty()) emptyList()
        else contactStore.fetchContactGroups(GroupsPredicate.GroupLookup(contactGroupIds))
            .asFlow()
            .flowOn(dispatchers.io)
            .firstOrNull()
            .orEmpty()
            .also { logger.debug("Found ${it.size} contact-groups for contact ${contact?.contactId}") }
    }

    private fun loadContacts(): ResourceFlow<List<IContactBase>> = flow {
        measureTimeMillis {
            val contacts = createContactsBaseFlow()
            emitAll(contacts)
        }.also { duration -> logger.debug("Loading android contacts took $duration ms") }
    }.toResourceFlow()

    private fun searchContacts(query: String): ResourceFlow<List<IContactBase>> = flow {
        measureTimeMillis {
            val predicate = ContactPredicate.NameLookup(query) // this actually searches over all fields
            val contacts = createContactsBaseFlow(predicate).firstOrNull()
            emit(contacts.orEmpty())
        }.also { duration -> logger.debug("Loading android contacts for query '$query' took $duration ms") }
    }.toResourceFlow()

    private fun createContactsBaseFlow(
        predicate: ContactPredicate? = null
    ): Flow<List<IContactBase>> {
        checkContactReadPermission { exception ->
            return flow {
                ErrorResource<List<IContactBase>>(listOf(exception))
            }
        }

        val displayNameStyle = if (Settings.current.orderByFirstName) Primary else Alternative
        val androidContacts = contactStore.fetchContacts(
            predicate = predicate,
            displayNameStyle = displayNameStyle,
        ).asFlow()

        val contacts = androidContacts.map { contacts ->
            logger.debug("Loaded ${contacts.size} android contacts with predicate $predicate")
            contacts.mapNotNull { it.toContactBase(rethrowExceptions = false) }
                .filter { validationService.validateContactBase(it).valid }
        }.flowOn(dispatchers.io)

        return contacts
    }

    private inline fun <T> checkContactReadPermission(permissionDeniedHandler: (MissingPermissionException) -> T) {
        if (!permissionService.hasContactReadPermission()) {
            val errorMessage = "Trying to load android contacts without read-permission.".also { logger.warning(it) }
            permissionDeniedHandler(MissingPermissionException(errorMessage))
        }
    }
}
