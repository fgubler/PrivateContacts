/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.mapping

import ch.abwesend.privatecontacts.domain.model.contact.IContactIdExternal
import ch.abwesend.privatecontacts.domain.service.interfaces.IAddressFormattingService
import ch.abwesend.privatecontacts.domain.service.interfaces.TelephoneService
import ch.abwesend.privatecontacts.infrastructure.service.addressformatting.AddressFormattingService
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someAndroidContact
import ch.abwesend.privatecontacts.testutil.databuilders.someAndroidContactGroup
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module
import java.util.Locale

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class AndroidContactMapperTest : TestBase() {
    private val addressFormattingService: IAddressFormattingService = AddressFormattingService()

    @MockK
    private lateinit var telephoneService: TelephoneService

    @MockK
    private lateinit var contactDataFactory: AndroidContactDataMapper

    private lateinit var underTest: AndroidContactMapper

    override fun setup() {
        super.setup()
        underTest = AndroidContactMapper()
        every { telephoneService.telephoneDefaultCountryIso } returns Locale.getDefault().country.lowercase()
        every { telephoneService.formatPhoneNumberForDisplay(any()) } answers { firstArg() }
        every { telephoneService.formatPhoneNumberForMatching(any()) } answers { firstArg() }
    }

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { addressFormattingService }
        module.single { telephoneService }
        module.single { contactDataFactory }
    }

    @Test
    fun `should create a ContactBase`() {
        val contactId = 433L
        val displayName = "Darth Vader"
        val androidContact = someAndroidContact(contactId = contactId, displayName = displayName)

        val result = underTest.toContactBase(contact = androidContact, rethrowExceptions = true)

        assertThat(result).isNotNull
        assertThat(result!!.id).isInstanceOf(IContactIdExternal::class.java)
        assertThat((result.id as IContactIdExternal).contactNo).isEqualTo(contactId)
        assertThat(result.displayName).isEqualTo(displayName)
    }

    @Test
    fun `should create a full Contact`() {
        val androidContact = someAndroidContact(
            contactId = 433L,
            firstName = "Gabriel",
            lastName = "De Leon",
            nickName = "Black Lion",
            note = "likes silver",
            organisation = "The Silver Saints"
        )
        val contactGroups = listOf(
            someAndroidContactGroup(title = "Group 1"),
            someAndroidContactGroup(title = "Group 2"),
            someAndroidContactGroup(title = "Group 3"),
        )
        every { contactDataFactory.getContactData(any()) } returns emptyList()

        val result = underTest.toContact(
            contact = androidContact,
            groups = contactGroups,
            rethrowExceptions = true
        )

        assertThat(result).isNotNull
        assertThat(result!!.id).isInstanceOf(IContactIdExternal::class.java)
        assertThat((result.id as IContactIdExternal).contactNo).isEqualTo(androidContact.contactId)
        assertThat(result.firstName).isEqualTo(androidContact.firstName)
        assertThat(result.lastName).isEqualTo(androidContact.lastName)
        assertThat(result.nickname).isEqualTo(androidContact.nickname)
        assertThat(result.notes).isEqualTo(androidContact.note?.raw)
        assertThat(result.contactGroups.map { it.id.name }).isEqualTo(contactGroups.map { it.title })
        assertThat(result.contactGroups.map { it.notes }).isEqualTo(contactGroups.map { it.note })
        verify { contactDataFactory.getContactData(androidContact) }
        assertThat(result.contactDataSet).hasSize(1)
        assertThat(result.contactDataSet.first().value).isEqualTo(androidContact.organization)
    }

    @Test
    fun `should write the middle name to the end of the first name`() {
        val androidContact = someAndroidContact(
            contactId = 433L,
            firstName = "Gabriel",
            middleName = "Something",
            lastName = "De Leon",
            nickName = "Black Lion",
            note = "likes silver",
        )
        every { contactDataFactory.getContactData(any()) } returns emptyList()

        val result = underTest.toContact(
            contact = androidContact,
            groups = emptyList(),
            rethrowExceptions = true,
        )

        assertThat(result).isNotNull
        assertThat(result!!.id).isInstanceOf(IContactIdExternal::class.java)
        assertThat(result.firstName).isEqualTo("Gabriel Something")
    }
}
