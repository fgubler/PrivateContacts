/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository

import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.contact.IContactBase
import ch.abwesend.privatecontacts.domain.repository.IAndroidContactRepository
import ch.abwesend.privatecontacts.domain.service.interfaces.PermissionService
import ch.abwesend.privatecontacts.domain.util.getAnywhere
import ch.abwesend.privatecontacts.domain.util.injectAnywhere
import com.alexstyl.contactstore.ContactStore
import com.alexstyl.contactstore.coroutines.asFlow
import kotlinx.coroutines.flow.take

/**
 * Repository to access the android ContactsProvider
 */
class AndroidContactRepository : IAndroidContactRepository {
    private val permissionService: PermissionService by injectAnywhere()

    private val contactStore: ContactStore by lazy {
        ContactStore.newInstance(getAnywhere())
    }

    override suspend fun loadContacts(): List<IContactBase> {
        if (!permissionService.hasContactReadPermission()) {
            logger.warning("Trying to load android contacts without read-permission.")
            return emptyList()
        }

        contactStore.fetchContacts().asFlow().take(1).collect { contacts ->
            logger.debug("Loaded ${contacts.size} contacts: $contacts")
        }

        logger.debug("Finished loading contacts")
        // TODO implement
        return emptyList()
    }
}
