/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts

import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.flow.ErrorResource
import ch.abwesend.privatecontacts.domain.lib.flow.ResourceFlow
import ch.abwesend.privatecontacts.domain.lib.flow.toResourceFlow
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactBase
import ch.abwesend.privatecontacts.domain.model.contact.ContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdAndroid
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContact
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.model.permission.MissingPermissionException
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig.All
import ch.abwesend.privatecontacts.domain.model.search.ContactSearchConfig.Query
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactRepository
import ch.abwesend.privatecontacts.domain.service.ContactValidationService
import ch.abwesend.privatecontacts.domain.service.interfaces.PermissionService
import ch.abwesend.privatecontacts.domain.service.valid
import ch.abwesend.privatecontacts.domain.settings.Settings
import ch.abwesend.privatecontacts.domain.util.getAnywhere
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import com.alexstyl.contactstore.ContactPredicate
import com.alexstyl.contactstore.ContactPredicate.ContactLookup
import com.alexstyl.contactstore.ContactStore
import com.alexstyl.contactstore.DisplayNameStyle.Alternative
import com.alexstyl.contactstore.DisplayNameStyle.Primary
import com.alexstyl.contactstore.allContactColumns
import com.alexstyl.contactstore.coroutines.asFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis
import com.alexstyl.contactstore.Contact as AndroidContact

/**
 * Repository to access the android ContactsProvider
 */
class AndroidContactRepository : IAndroidContactRepository {
    private val permissionService: PermissionService by injectAnywhere()
    private val validationService: ContactValidationService by injectAnywhere()
    private val dispatchers: IDispatchers by injectAnywhere()

    private val contactStore: ContactStore by lazy {
        ContactStore.newInstance(getAnywhere())
    }

    override suspend fun loadContactsAsFlow(searchConfig: ContactSearchConfig): ResourceFlow<List<IContactBase>> =
        when (searchConfig) {
            is All -> loadContacts()
            is Query -> searchContacts(searchConfig.query)
        }

    override suspend fun resolveContact(contactId: IContactIdExternal): IContact {
        checkContactReadPermission { exception -> throw exception }
        val contactRaw = contactStore.fetchContacts(
            predicate = ContactLookup(contactId = contactId.contactNo),
            columnsToFetch = allContactColumns()
        ).asFlow().firstOrNull()?.firstOrNull()

        return contactRaw?.toContact() ?: throw IllegalArgumentException("Contact $contactId not found on android")
    }

    private suspend fun loadContacts(): ResourceFlow<List<IContactBase>> = flow {
        measureTimeMillis {
            val contacts = createContactsBaseFlow().firstOrNull()
            emit(contacts.orEmpty())
        }.also { duration -> logger.debug("Loading android contacts took $duration ms") }
    }.toResourceFlow()

    private suspend fun searchContacts(query: String): ResourceFlow<List<IContactBase>> = flow {
        measureTimeMillis {
            // no caching is needed because it should be fast enough thanks to filtering
            val predicate = ContactPredicate.NameLookup(query) // this actually searches over all fields
            val contacts = createContactsBaseFlow(predicate).firstOrNull()
            emit(contacts.orEmpty())
        }.also { duration -> logger.debug("Loading android contacts for query '$query' took $duration ms") }
    }.toResourceFlow()

    private suspend fun createContactsBaseFlow(
        predicate: ContactPredicate? = null
    ): Flow<List<IContactBase>> = withContext(dispatchers.io) {
        checkContactReadPermission { exception ->
            return@withContext flow {
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
            contacts.mapNotNull { it.toContactBase() }
                .filter { validationService.validateContactBase(it).valid }
        }
        contacts
    }

    private inline fun <T> checkContactReadPermission(permissionDeniedHandler: (MissingPermissionException) -> T) {
        if (!permissionService.hasContactReadPermission()) {
            val errorMessage = "Trying to load android contacts without read-permission.".also { logger.warning(it) }
            permissionDeniedHandler(MissingPermissionException(errorMessage))
        }
    }
}

private fun AndroidContact.toContactBase(): IContactBase? =
    try {
        ContactBase(
            id = ContactIdAndroid(contactNo = contactId),
            type = ContactType.PUBLIC,
            displayName = displayName,
        )
    } catch (t: Throwable) {
        logger.warning("Failed to map android contact with id = $contactId", t)
        null
    }

private fun AndroidContact.toContact(): IContact? =
    try {
        ContactEditable(
            id = ContactIdAndroid(contactNo = contactId),
            type = ContactType.PUBLIC,
            firstName = firstName,
            lastName = lastName,
            nickname = nickname,
            notes = note?.raw.orEmpty(),
            contactDataSet = mutableListOf(), // TODO read contact-data
        )
    } catch (t: Throwable) {
        logger.warning("Failed to map android contact with id = $contactId", t)
        null
    }