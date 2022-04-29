/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.database

import ch.abwesend.privatecontacts.BuildConfig
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.contact.Contact
import ch.abwesend.privatecontacts.domain.model.contact.ContactId
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contactdata.Company
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataId
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.util.getAnywhere
import ch.abwesend.privatecontacts.infrastructure.room.contact.ContactDao

class DatabaseInitializer {
    suspend fun initializeDatabase(contactDao: ContactDao): Boolean = try {
        val hasData = contactDao.count() > 0

        if (!hasData && BuildConfig.DEBUG) {
            logger.debug("Initializing database")
            val contactRepository: IContactRepository = getAnywhere()
            dummyContacts.forEach { contactRepository.createContact(it) }
            logger.debug("Initialized database successfully")
        }

        true
    } catch (e: Exception) {
        logger.error("Failed to initialize database", e)
        false
    }

    private val dummyContacts
        get() = mutableListOf(
            Contact(
                id = ContactId.randomId(),
                firstName = "Darth",
                lastName = "Vader",
                nickname = "Darthy",
                type = ContactType.SECRET,
                notes = "Evil but not very good at it",
                contactDataSet = mutableListOf(),
            ),
            Contact(
                id = ContactId.randomId(),
                firstName = "Luke",
                lastName = "Skywalker",
                nickname = "Lucky Luke",
                type = ContactType.PUBLIC,
                notes = "Lost his hand",
                contactDataSet = mutableListOf(
                    PhoneNumber(
                        id = ContactDataId.randomId(),
                        value = "(650) 555-1212",
                        type = ContactDataType.Personal,
                        isMain = true,
                        sortOrder = 0,
                        modelStatus = ModelStatus.CHANGED,
                    ),
                    EmailAddress(
                        id = ContactDataId.randomId(),
                        value = "luke@jedi.com",
                        type = ContactDataType.Business,
                        isMain = true,
                        sortOrder = 0,
                        modelStatus = ModelStatus.CHANGED,
                    ),
                    PhysicalAddress(
                        id = ContactDataId.randomId(),
                        value = "A lonely hut",
                        type = ContactDataType.Personal,
                        isMain = true,
                        sortOrder = 0,
                        modelStatus = ModelStatus.CHANGED,
                    ),
                    Company(
                        id = ContactDataId.randomId(),
                        value = "Jedi Inc.",
                        type = ContactDataType.Other,
                        isMain = true,
                        sortOrder = 0,
                        modelStatus = ModelStatus.CHANGED,
                    )
                ),
            ),
            Contact(
                id = ContactId.randomId(),
                firstName = "Obi-Wan",
                lastName = "Kenobi",
                nickname = "Obi",
                type = ContactType.PUBLIC,
                notes = "Efficient way of suicide",
                contactDataSet = mutableListOf(
                    PhoneNumber(
                        id = ContactDataId.randomId(),
                        value = "12345",
                        type = ContactDataType.Personal,
                        isMain = true,
                        modelStatus = ModelStatus.CHANGED,
                        sortOrder = 0,
                    ),
                    PhoneNumber(
                        id = ContactDataId.randomId(),
                        value = "123456",
                        type = ContactDataType.Business,
                        isMain = false,
                        modelStatus = ModelStatus.CHANGED,
                        sortOrder = 1,
                    ),
                ),
            ),
            Contact(
                id = ContactId.randomId(),
                firstName = "Yoda",
                lastName = "",
                nickname = "Yo-Da",
                type = ContactType.SECRET,
                notes = "Small and green",
                contactDataSet = mutableListOf(
                    PhoneNumber(
                        id = ContactDataId.randomId(),
                        value = "123456",
                        type = ContactDataType.Personal,
                        isMain = false,
                        modelStatus = ModelStatus.CHANGED,
                        sortOrder = 0,
                    ),
                    PhoneNumber(
                        id = ContactDataId.randomId(),
                        value = "1234567",
                        type = ContactDataType.Business,
                        isMain = false,
                        modelStatus = ModelStatus.CHANGED,
                        sortOrder = 1,
                    ),
                    PhoneNumber(
                        id = ContactDataId.randomId(),
                        value = "12345678",
                        type = ContactDataType.CustomValue("Jedi-Number"),
                        isMain = true,
                        modelStatus = ModelStatus.CHANGED,
                        sortOrder = 2,
                    ),
                ),
            ),
        )
}
