/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.lib.coroutine.IDispatchers
import ch.abwesend.privatecontacts.domain.lib.flow.ErrorResource
import ch.abwesend.privatecontacts.domain.lib.flow.ResourceFlow
import ch.abwesend.privatecontacts.domain.lib.flow.toResourceFlow
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactBase
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdAndroid
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.model.permission.MissingPermissionException
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactRepository
import ch.abwesend.privatecontacts.domain.service.ContactValidationService
import ch.abwesend.privatecontacts.domain.service.interfaces.PermissionService
import ch.abwesend.privatecontacts.domain.service.valid
import ch.abwesend.privatecontacts.domain.util.getAnywhere
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import com.alexstyl.contactstore.ContactColumn
import com.alexstyl.contactstore.ContactStore
import com.alexstyl.contactstore.coroutines.asFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
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

    // TODO re-think this. This only works while the user cannot change android contacts.
    private var allContactsCached: List<IContactBase>? = null

    override suspend fun loadContactsAsFlow(): ResourceFlow<List<IContactBase>> = flow {
        val contacts = allContactsCached ?: createAllContactsFlow().first().also { allContactsCached = it }
        emit(contacts)
    }.toResourceFlow()

    private suspend fun createAllContactsFlow(): Flow<List<IContactBase>> = withContext(dispatchers.io) {
        if (!permissionService.hasContactReadPermission()) {
            val errorMessage = "Trying to load android contacts without read-permission.".also { logger.warning(it) }
            return@withContext flow {
                ErrorResource<List<IContactBase>>(listOf(MissingPermissionException(errorMessage)))
            }
        }

        val androidContacts = contactStore.fetchContacts(columnsToFetch = listOf(ContactColumn.Names)).asFlow()

        val contacts = androidContacts.map { contacts ->
            logger.debug("Loaded ${contacts.size} android contacts: $androidContacts")
            contacts.mapNotNull { it.toContactBase() }
                .filter { validationService.validateContactBase(it).valid }
        }
        contacts
    }
}

private fun AndroidContact.toContactBase(): IContactBase? =
    try {
        ContactBase(
            id = ContactIdAndroid(contactNo = contactId),
            type = ContactType.PUBLIC,
            firstName = firstName,
            lastName = lastName,
        )
    } catch (t: Throwable) {
        logger.warning("Failed to map android contact with id = $contactId", t)
        null
    }
