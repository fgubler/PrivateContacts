package ch.abwesend.privatecontacts.infrastructure.room.database

import android.content.Context
import androidx.room.Room
import ch.abwesend.privatecontacts.domain.model.contact.ContactEditable
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataSubType
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.util.getAnywhere
import java.util.UUID

interface IDatabaseFactory {
    fun createDatabase(context: Context): AppDatabase
    suspend fun initializeDatabase()
}

open class DatabaseFactory : IDatabaseFactory {
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
        dummyContacts.forEach { contactRepository.createContact(it) }
    }
}

val dummyContacts = mutableListOf(
    ContactEditable(
        id = UUID.randomUUID(),
        firstName = "Darth",
        lastName = "Vader",
        nickname = "Darthy",
        type = ContactType.PRIVATE,
        notes = "Evil but not very good at it",
        phoneNumbers = mutableListOf(),
    ),
    ContactEditable(
        id = UUID.randomUUID(),
        firstName = "Luke",
        lastName = "Skywalker",
        nickname = "Lucky Luke",
        type = ContactType.PUBLIC,
        notes = "Lost his hand",
        phoneNumbers = mutableListOf(
            PhoneNumber(
                value = "1234",
                type = ContactDataSubType.Private,
                isMainNumber = true,
            ),
        ),
    ),
    ContactEditable(
        id = UUID.randomUUID(),
        firstName = "Obi-Wan",
        lastName = "Kenobi",
        nickname = "Obi",
        type = ContactType.PUBLIC,
        notes = "Efficient way of suicide",
        phoneNumbers = mutableListOf(
            PhoneNumber(
                value = "12345",
                type = ContactDataSubType.Private,
                isMainNumber = true,
            ),
            PhoneNumber(
                value = "123456",
                type = ContactDataSubType.Business,
                isMainNumber = false,
            ),
        ),
    ),
    ContactEditable(
        id = UUID.randomUUID(),
        firstName = "Yoda",
        lastName = "",
        nickname = "Yo-Da",
        type = ContactType.PRIVATE,
        notes = "Small and green",
        phoneNumbers = mutableListOf(
            PhoneNumber(
                value = "123456",
                type = ContactDataSubType.Private,
                isMainNumber = false,
            ),
            PhoneNumber(
                value = "1234567",
                type = ContactDataSubType.Business,
                isMainNumber = false,
            ),
            PhoneNumber(
                value = "12345678",
                type = ContactDataSubType.Custom("Jedi-Number"),
                isMainNumber = true,
            ),
        ),
    ),
)
