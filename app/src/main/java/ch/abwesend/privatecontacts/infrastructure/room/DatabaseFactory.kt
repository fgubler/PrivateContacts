package ch.abwesend.privatecontacts.infrastructure.room

import android.content.Context
import androidx.room.Room
import ch.abwesend.privatecontacts.BuildConfig
import ch.abwesend.privatecontacts.domain.model.ContactFull
import ch.abwesend.privatecontacts.domain.model.ContactType
import ch.abwesend.privatecontacts.domain.model.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.PhoneNumberType
import java.util.UUID

object DatabaseFactory {
    fun createDatabase(context: Context,): AppDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "private_contacts_database"
        )
            .let {
                if (BuildConfig.DEBUG) it.fallbackToDestructiveMigration() // recreate DB if migrations fail
                else it
            }
            .addMigrations(*DatabaseMigrations.allMigrations)
            .build()
}

val dummyContacts = listOf(
    ContactFull(
        id = 1,
        uuid = UUID.randomUUID(),
        firstName = "Darth",
        lastName = "Vader",
        nickname = "Darthy",
        type = ContactType.PRIVATE,
        notes = "Evil but not very good at it",
        phoneNumbers = listOf(),
    ),
    ContactFull(
        id = 2,
        uuid = UUID.randomUUID(),
        firstName = "Luke",
        lastName = "Skywalker",
        nickname = "Lucky Luke",
        type = ContactType.PUBLIC,
        notes = "Lost his hand",
        phoneNumbers = listOf(
            PhoneNumber(
                value = "1234",
                type = PhoneNumberType.Private
            ),
        ),
    ),
    ContactFull(
        id = 3,
        uuid = UUID.randomUUID(),
        firstName = "Obi-Wan",
        lastName = "Kenobi",
        nickname = "Obi",
        type = ContactType.PUBLIC,
        notes = "Efficient way of suicide",
        phoneNumbers = listOf(
            PhoneNumber(
                value = "12345",
                type = PhoneNumberType.Private
            ),
            PhoneNumber(
                value = "123456",
                type = PhoneNumberType.Business
            ),
        ),
    ),
    ContactFull(
        id = 4,
        uuid = UUID.randomUUID(),
        firstName = "Yoda",
        lastName = "",
        nickname = "Yo-Da",
        type = ContactType.PRIVATE,
        notes = "Small and green",
        phoneNumbers = listOf(
            PhoneNumber(
                value = "123456",
                type = PhoneNumberType.Private
            ),
            PhoneNumber(
                value = "1234567",
                type = PhoneNumberType.Business
            ),
            PhoneNumber(
                value = "12345678",
                type = PhoneNumberType.Custom("Jedi-Number")
            ),
        ),
    ),
)
