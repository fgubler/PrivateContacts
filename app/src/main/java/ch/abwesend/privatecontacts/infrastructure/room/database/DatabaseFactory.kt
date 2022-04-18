/*
 * Private Contacts
 * Copyright (c) 2022.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.room.database

import android.content.Context
import androidx.room.Room
import ch.abwesend.privatecontacts.BuildConfig
import ch.abwesend.privatecontacts.domain.model.ModelStatus.CHANGED
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

interface IDatabaseFactory {
    fun createDatabase(context: Context): AppDatabase
    suspend fun initializeDatabase()
}

object DatabaseFactory : IDatabaseFactory {
    override fun createDatabase(context: Context): AppDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "private_contacts_database"
        )
            // .fallbackToDestructiveMigration() // insert to recreate DB if migrations fail
            .addMigrations(*DatabaseMigrations.allMigrations)
            .build()

    override suspend fun initializeDatabase() {
        val contactRepository: IContactRepository = getAnywhere()

        if (BuildConfig.DEBUG) {
            dummyContacts.forEach { contactRepository.createContact(it) }
        }
    }
}

val dummyContacts
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
                    value = "1234",
                    type = ContactDataType.Personal,
                    isMain = true,
                    sortOrder = 0,
                    modelStatus = CHANGED,
                ),
                EmailAddress(
                    id = ContactDataId.randomId(),
                    value = "luke@jedi.com",
                    type = ContactDataType.Business,
                    isMain = true,
                    sortOrder = 0,
                    modelStatus = CHANGED,
                ),
                PhysicalAddress(
                    id = ContactDataId.randomId(),
                    value = "A lonely hut",
                    type = ContactDataType.Personal,
                    isMain = true,
                    sortOrder = 0,
                    modelStatus = CHANGED,
                ),
                Company(
                    id = ContactDataId.randomId(),
                    value = "Jedi Inc.",
                    type = ContactDataType.Other,
                    isMain = true,
                    sortOrder = 0,
                    modelStatus = CHANGED,
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
                    modelStatus = CHANGED,
                    sortOrder = 0,
                ),
                PhoneNumber(
                    id = ContactDataId.randomId(),
                    value = "123456",
                    type = ContactDataType.Business,
                    isMain = false,
                    modelStatus = CHANGED,
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
                    modelStatus = CHANGED,
                    sortOrder = 0,
                ),
                PhoneNumber(
                    id = ContactDataId.randomId(),
                    value = "1234567",
                    type = ContactDataType.Business,
                    isMain = false,
                    modelStatus = CHANGED,
                    sortOrder = 1,
                ),
                PhoneNumber(
                    id = ContactDataId.randomId(),
                    value = "12345678",
                    type = ContactDataType.CustomValue("Jedi-Number"),
                    isMain = true,
                    modelStatus = CHANGED,
                    sortOrder = 2,
                ),
            ),
        ),
    )
