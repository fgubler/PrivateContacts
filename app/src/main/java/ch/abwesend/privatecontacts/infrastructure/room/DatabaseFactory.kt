package ch.abwesend.privatecontacts.infrastructure.room

import android.content.Context
import androidx.room.Room
import ch.abwesend.privatecontacts.domain.model.ContactFull
import ch.abwesend.privatecontacts.domain.model.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.PhoneNumberType

object DatabaseFactory {
    fun createDatabase(context: Context,): AppDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "private_contacts_database"
        ).build()
}

val dummyContacts = listOf(
    ContactFull(
        id = 1,
        firstName = "Darth",
        lastName = "Vader",
        nickname = "Darthy",
        notes = "Evil but not very good at it",
        phoneNumbers = listOf(),
    ),
    ContactFull(
        id = 2,
        firstName = "Luke",
        lastName = "Skywalker",
        nickname = "Lucky Luke",
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
        firstName = "Obi-Wan",
        lastName = "Kenobi",
        nickname = "Obi",
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
        firstName = "Yoda",
        lastName = "",
        nickname = "Yo-Da",
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
