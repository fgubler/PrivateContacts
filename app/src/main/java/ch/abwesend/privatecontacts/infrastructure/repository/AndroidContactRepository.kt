/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.ContactBase
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdAndroid
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactRepository
import ch.abwesend.privatecontacts.domain.service.ContactValidationService
import ch.abwesend.privatecontacts.domain.service.interfaces.PermissionService
import ch.abwesend.privatecontacts.domain.service.valid
import ch.abwesend.privatecontacts.domain.util.getAnywhere
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import com.alexstyl.contactstore.ContactColumn
import com.alexstyl.contactstore.ContactStore
import com.alexstyl.contactstore.coroutines.asFlow
import kotlinx.coroutines.flow.firstOrNull
import com.alexstyl.contactstore.Contact as AndroidContact

/**
 * Repository to access the android ContactsProvider
 */
class AndroidContactRepository : IAndroidContactRepository {
    private val permissionService: PermissionService by injectAnywhere()
    private val validationService: ContactValidationService by injectAnywhere()

    private val contactStore: ContactStore by lazy {
        ContactStore.newInstance(getAnywhere())
    }

    override suspend fun loadContacts(): List<IContactBase> {
        if (!permissionService.hasContactReadPermission()) {
            logger.warning("Trying to load android contacts without read-permission.")
            return emptyList()
        }

        val contacts = contactStore.fetchContacts(columnsToFetch = listOf(ContactColumn.Names))
            .asFlow()
            .firstOrNull()
            .orEmpty()

        logger.debug("Loaded ${contacts.size} contacts: $contacts")

        return contacts
            .mapNotNull { it.toContactBase() }
            .filter { validationService.validateContactBase(it).valid }
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
