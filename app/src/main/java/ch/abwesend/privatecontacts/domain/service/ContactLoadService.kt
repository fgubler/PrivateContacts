package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.lib.flow.ResourceStateFlow
import ch.abwesend.privatecontacts.domain.lib.flow.emitReady
import ch.abwesend.privatecontacts.domain.lib.flow.mutableResourceStateFlow
import ch.abwesend.privatecontacts.domain.model.Contact
import ch.abwesend.privatecontacts.domain.model.ContactRead
import ch.abwesend.privatecontacts.domain.model.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.PhoneNumberType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

interface IContactLoadService {
    fun loadContacts(): ResourceStateFlow<List<Contact>>
}

class ContactLoadService : IContactLoadService {
    override fun loadContacts(): ResourceStateFlow<List<Contact>> {
        // TODO implement
        val stateFlow = mutableResourceStateFlow<List<Contact>>()

        CoroutineScope(Dispatchers.Default).launch {
            stateFlow.emitReady(dummyContacts)
        }

        return stateFlow
    }
}

private val dummyContacts = listOf(
    ContactRead(
        id = UUID.randomUUID(),
        firstName = "Darth",
        lastName = "Vader",
        phoneNumbers = listOf(),
    ),
    ContactRead(
        id = UUID.randomUUID(),
        firstName = "Luke",
        lastName = "Skywalker",
        phoneNumbers = listOf(
            PhoneNumber(
                value = "1234",
                type = PhoneNumberType.Private
            ),
        ),
    ),
    ContactRead(
        id = UUID.randomUUID(),
        firstName = "Obi-Wan",
        lastName = "Kenobi",
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
    ContactRead(
        id = UUID.randomUUID(),
        firstName = "Yoda",
        lastName = "",
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
