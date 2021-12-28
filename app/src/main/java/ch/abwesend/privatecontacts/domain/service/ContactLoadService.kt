package ch.abwesend.privatecontacts.domain.service

import ch.abwesend.privatecontacts.domain.lib.flow.ResourceStateFlow
import ch.abwesend.privatecontacts.domain.lib.flow.emitLoading
import ch.abwesend.privatecontacts.domain.lib.flow.emitReady
import ch.abwesend.privatecontacts.domain.lib.flow.mutableResourceStateFlow
import ch.abwesend.privatecontacts.domain.lib.logging.logger
import ch.abwesend.privatecontacts.domain.model.ContactBase
import ch.abwesend.privatecontacts.domain.model.ContactFull
import ch.abwesend.privatecontacts.domain.model.PhoneNumber
import ch.abwesend.privatecontacts.domain.model.PhoneNumberType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

interface IContactLoadService {
    fun loadContacts(): ResourceStateFlow<List<ContactBase>>
}

class ContactLoadService : IContactLoadService {
    override fun loadContacts(): ResourceStateFlow<List<ContactBase>> {
        // TODO implement
        val stateFlow = mutableResourceStateFlow<List<ContactBase>>()

        CoroutineScope(Dispatchers.Default).launch {
            logger.debug("contacts: before emission")
            stateFlow.emitLoading()
            logger.debug("contacts: emitted loading")
            delay(2000)
            stateFlow.emitReady(dummyContacts)
            logger.debug("contacts: emitted ready")
        }

        return stateFlow
    }
}

private val dummyContacts = listOf(
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
