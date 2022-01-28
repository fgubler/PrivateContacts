package ch.abwesend.privatecontacts.infrastructure.room.database

import android.content.Context
import androidx.room.Room
import ch.abwesend.privatecontacts.BuildConfig
import ch.abwesend.privatecontacts.domain.model.ModelStatus.CHANGED
import ch.abwesend.privatecontacts.domain.model.contact.ContactFull
import ch.abwesend.privatecontacts.domain.model.contact.ContactType
import ch.abwesend.privatecontacts.domain.model.contactdata.ContactDataType
import ch.abwesend.privatecontacts.domain.model.contactdata.PhoneNumber
import ch.abwesend.privatecontacts.domain.repository.IContactRepository
import ch.abwesend.privatecontacts.domain.util.getAnywhere
import java.util.UUID

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

val dummyContacts = mutableListOf(
    ContactFull(
        id = UUID.randomUUID(),
        firstName = "Darth",
        lastName = "Vader",
        nickname = "Darthy",
        type = ContactType.PRIVATE,
        notes = "Evil but not very good at it",
        contactDataSet = mutableListOf(),
    ),
    ContactFull(
        id = UUID.randomUUID(),
        firstName = "Luke",
        lastName = "Skywalker",
        nickname = "Lucky Luke",
        type = ContactType.PUBLIC,
        notes = "Lost his hand",
        contactDataSet = mutableListOf(
            PhoneNumber(
                id = UUID.randomUUID(),
                value = "1234",
                type = ContactDataType.Private,
                isMain = true,
                sortOrder = 0,
                modelStatus = CHANGED,
            ),
        ),
    ),
    ContactFull(
        id = UUID.randomUUID(),
        firstName = "Obi-Wan",
        lastName = "Kenobi",
        nickname = "Obi",
        type = ContactType.PUBLIC,
        notes = "Efficient way of suicide",
        contactDataSet = mutableListOf(
            PhoneNumber(
                id = UUID.randomUUID(),
                value = "12345",
                type = ContactDataType.Private,
                isMain = true,
                modelStatus = CHANGED,
                sortOrder = 0,
            ),
            PhoneNumber(
                id = UUID.randomUUID(),
                value = "123456",
                type = ContactDataType.Business,
                isMain = false,
                modelStatus = CHANGED,
                sortOrder = 1,
            ),
        ),
    ),
    ContactFull(
        id = UUID.randomUUID(),
        firstName = "Yoda",
        lastName = "",
        nickname = "Yo-Da",
        type = ContactType.PRIVATE,
        notes = "Small and green",
        contactDataSet = mutableListOf(
            PhoneNumber(
                id = UUID.randomUUID(),
                value = "123456",
                type = ContactDataType.Private,
                isMain = false,
                modelStatus = CHANGED,
                sortOrder = 0,
            ),
            PhoneNumber(
                id = UUID.randomUUID(),
                value = "1234567",
                type = ContactDataType.Business,
                isMain = false,
                modelStatus = CHANGED,
                sortOrder = 1,
            ),
            PhoneNumber(
                id = UUID.randomUUID(),
                value = "12345678",
                type = ContactDataType.CustomValue("Jedi-Number"),
                isMain = true,
                modelStatus = CHANGED,
                sortOrder = 2,
            ),
        ),
    ),
)
