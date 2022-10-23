/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.database

import ch.abwesend.privatecontacts.BuildConfig
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.ModelStatus
import ch.abwesend.privatecontacts.domain.model.contact.ContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contact.ContactType.SECRET
import ch.abwesend.privatecontacts.domain.model.contact.IContactIdInternal
import ch.abwesend.privatecontacts.domain.model.contactdata.Company
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.EmailAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.EventDate
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.contactdata.PhysicalAddress
import ch.abwesend.privatecontacts.domain.model.contactdata.Relationship
import ch.abwesend.privatecontacts.domain.model.contactdata.createContactDataId
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroup
import ch.abwesend.privatecontacts.domain.model.contactgroup.ContactGroupId
import ch.abwesend.privatecontacts.domain.model.contactimage.ContactImage
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.util.getAnywhere
import ch.abwesend.privatecontacts.infrastructure.room.contact.ContactDao
import java.time.LocalDate

class DatabaseInitializer {
    suspend fun initializeDatabase(contactDao: ContactDao): Boolean = try {
        val hasData = contactDao.count() > 0

        if (!hasData && BuildConfig.DEBUG) {
            logger.debug("Initializing database")
            val contactRepository: IContactRepository = getAnywhere()
            dummyContacts.forEach { contact ->
                val contactId = (contact.id as? IContactIdInternal) ?: ContactIdInternal.randomId()
                contactRepository.createContact(contactId, contact)
            }
            logger.debug("Initialized database successfully")
        }

        true
    } catch (e: Exception) {
        logger.error("Failed to initialize database", e)
        false
    }

    private val dummyContacts
        get() = mutableListOf(
            ContactEditable(
                id = ContactIdInternal.randomId(),
                firstName = "Darth",
                lastName = "Vader",
                nickname = "Darthy",
                type = SECRET,
                notes = "Evil but not very good at it",
                image = ContactImage.empty,
                contactDataSet = mutableListOf(),
                contactGroups = mutableListOf(),
            ),
            ContactEditable(
                id = ContactIdInternal.randomId(),
                firstName = "Luke",
                lastName = "Skywalker",
                nickname = "Lucky Luke",
                type = SECRET,
                notes = "Lost his hand",
                image = ContactImage(
                    thumbnailUri = "Pseudo Thumbnail",
                    fullImage = ByteArray(0),
                    modelStatus = ModelStatus.UNCHANGED
                ),
                contactDataSet = mutableListOf(
                    PhoneNumber(
                        id = createContactDataId(),
                        value = "(650) 555-1212",
                        type = ContactDataType.Personal,
                        isMain = true,
                        sortOrder = 0,
                        modelStatus = ModelStatus.CHANGED,
                    ),
                    EmailAddress(
                        id = createContactDataId(),
                        value = "luke@jedi.com",
                        type = ContactDataType.Business,
                        isMain = true,
                        sortOrder = 0,
                        modelStatus = ModelStatus.CHANGED,
                    ),
                    PhysicalAddress(
                        id = createContactDataId(),
                        value = "A lonely hut",
                        type = ContactDataType.Personal,
                        isMain = true,
                        sortOrder = 0,
                        modelStatus = ModelStatus.CHANGED,
                    ),
                    Company(
                        id = createContactDataId(),
                        value = "Jedi Inc.",
                        type = ContactDataType.Other,
                        isMain = true,
                        sortOrder = 0,
                        modelStatus = ModelStatus.CHANGED,
                    ),
                    EventDate(
                        id = createContactDataId(),
                        value = LocalDate.now(),
                        type = ContactDataType.Birthday,
                        isMain = true,
                        sortOrder = 0,
                        modelStatus = ModelStatus.CHANGED,
                    ),
                    EventDate(
                        id = createContactDataId(),
                        value = LocalDate.now().plusDays(5),
                        type = ContactDataType.Anniversary,
                        isMain = false,
                        sortOrder = 1,
                        modelStatus = ModelStatus.CHANGED,
                    ),
                    Relationship(
                        id = createContactDataId(),
                        value = "Darth Vader",
                        type = ContactDataType.RelationshipParent,
                        isMain = true,
                        sortOrder = 0,
                        modelStatus = ModelStatus.CHANGED,
                    ),
                    Relationship(
                        id = createContactDataId(),
                        value = "Leia Organa",
                        type = ContactDataType.RelationshipSibling,
                        isMain = false,
                        sortOrder = 1,
                        modelStatus = ModelStatus.CHANGED,
                    ),
                ),
                contactGroups = mutableListOf(
                    ContactGroup(id = ContactGroupId("Future TestUsers"), notes = "2D"),
                    ContactGroup(id = ContactGroupId("Random Dudes"), notes = ""),
                ),
            ),
            ContactEditable(
                id = ContactIdInternal.randomId(),
                firstName = "Obi-Wan",
                lastName = "Kenobi",
                nickname = "Obi",
                type = SECRET,
                notes = "Efficient way of suicide",
                image = ContactImage.empty,
                contactDataSet = mutableListOf(
                    PhoneNumber(
                        id = createContactDataId(),
                        value = "12345",
                        type = ContactDataType.Personal,
                        isMain = true,
                        modelStatus = ModelStatus.CHANGED,
                        sortOrder = 0,
                    ),
                    PhoneNumber(
                        id = createContactDataId(),
                        value = "123456",
                        type = ContactDataType.Business,
                        isMain = false,
                        modelStatus = ModelStatus.CHANGED,
                        sortOrder = 1,
                    ),
                ),
                contactGroups = mutableListOf(),
            ),
            ContactEditable(
                id = ContactIdInternal.randomId(),
                firstName = "Yoda",
                lastName = "",
                nickname = "Yo-Da",
                type = SECRET,
                notes = "Small and green",
                image = ContactImage.empty,
                contactDataSet = mutableListOf(
                    PhoneNumber(
                        id = createContactDataId(),
                        value = "123456",
                        type = ContactDataType.Personal,
                        isMain = false,
                        modelStatus = ModelStatus.CHANGED,
                        sortOrder = 0,
                    ),
                    PhoneNumber(
                        id = createContactDataId(),
                        value = "1234567",
                        type = ContactDataType.Business,
                        isMain = false,
                        modelStatus = ModelStatus.CHANGED,
                        sortOrder = 1,
                    ),
                    PhoneNumber(
                        id = createContactDataId(),
                        value = "12345678",
                        type = ContactDataType.CustomValue("Jedi-Number"),
                        isMain = true,
                        modelStatus = ModelStatus.CHANGED,
                        sortOrder = 2,
                    ),
                ),
                contactGroups = mutableListOf(),
            ),
        )
}
