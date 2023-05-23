/*
 * Private Contacts
 * Copyright (c) 2023.
 * Florian Gubler
 */

package ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.service

import ch.abwesend.privatecontacts.domain.model.contact.ContactAccount
import ch.abwesend.privatecontacts.domain.model.contact.ContactIdAndroid
import ch.abwesend.privatecontacts.domain.service.interfaces.IAddressFormattingService
import ch.abwesend.privatecontacts.domain.service.interfaces.TelephoneService
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.mapping.AndroidContactDataMapper
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.mapping.AndroidContactMapper
import ch.abwesend.privatecontacts.infrastructure.repository.androidcontacts.repository.AndroidContactLoadRepository
import ch.abwesend.privatecontacts.testutil.TestBase
import ch.abwesend.privatecontacts.testutil.databuilders.someAndroidContact
import ch.abwesend.privatecontacts.testutil.databuilders.someAndroidContactGroup
import ch.abwesend.privatecontacts.testutil.databuilders.someContactBase
import ch.abwesend.privatecontacts.testutil.databuilders.someExternalContactId
import ch.abwesend.privatecontacts.testutil.databuilders.someInternetAccount
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.module.Module

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class AndroidContactLoadServiceTest : TestBase() {
    @MockK
    private lateinit var contactLoadRepository: AndroidContactLoadRepository

    @MockK
    private lateinit var addressFormattingService: IAddressFormattingService

    @MockK
    private lateinit var telephoneService: TelephoneService

    @SpyK
    private var contactFactory: AndroidContactMapper = AndroidContactMapper()

    @SpyK
    private var contactDataFactory: AndroidContactDataMapper = AndroidContactDataMapper()

    @InjectMockKs
    private lateinit var underTest: AndroidContactLoadService

    override fun setupKoinModule(module: Module) {
        super.setupKoinModule(module)
        module.single { contactLoadRepository }
        module.single { telephoneService }
        module.single { addressFormattingService }
        module.single { contactFactory }
        module.single { contactDataFactory }
    }

    override fun setup() {
        super.setup()

        every {
            addressFormattingService.formatAddress(any(), any(), any(), any(), any(), any())
        } answers {
            val arguments: List<String> = args.filterIsInstance<String>()
            arguments.filter { it.isNotEmpty() }.joinToString(" ").trim()
        }
    }

    @Test
    fun `should resolve an existing contact by ID`() {
        val contactId = ContactIdAndroid(contactNo = 123123)
        val contact = someAndroidContact(contactId = contactId.contactNo, relaxed = true)
        coEvery { contactLoadRepository.resolveContactRaw(any()) } returns contact
        coEvery { contactLoadRepository.loadContactGroups(any()) } returns emptyList()

        val result = runBlocking { underTest.resolveContact(contactId) }

        coVerify { contactLoadRepository.resolveContactRaw(contactId) }
        assertThat(result.id).isEqualTo(contactId)
    }

    @Test
    fun `resolving a contact should re-throw if the contact could not be mapped`() {
        val contactId = ContactIdAndroid(contactNo = 123123)
        val exception = IllegalStateException("Just some test exception")
        val contact = someAndroidContact(contactId = contactId.contactNo, relaxed = true)
        coEvery { contactLoadRepository.resolveContactRaw(any()) } returns contact
        coEvery { contactLoadRepository.loadContactGroups(any()) } returns emptyList()
        every { contact.note } throws exception

        val thrownException = assertThrows<IllegalStateException> {
            runBlocking { underTest.resolveContact(contactId) }
        }

        assertThat(thrownException).isEqualTo(exception)
    }

    @Test
    fun `should check whether contacts exist`() {
        val existingContactIds = listOf(
            someExternalContactId(contactNo = 1),
            someExternalContactId(contactNo = 2),
            someExternalContactId(contactNo = 3),
            someExternalContactId(contactNo = 4),
        )
        val notExistingContactIds = listOf(
            someExternalContactId(contactNo = 40),
            someExternalContactId(contactNo = 41),
            someExternalContactId(contactNo = 42),
        )
        val existingContacts = existingContactIds.map { someContactBase(id = it) }
        val allContactsIds = existingContactIds + notExistingContactIds
        val expectedResult = existingContactIds.associateWith { true } + notExistingContactIds.associateWith { false }
        coEvery { contactLoadRepository.loadContactsSnapshot(any()) } returns existingContacts

        val result = runBlocking { underTest.doContactsExist(allContactsIds.toSet()) }

        coVerify { contactLoadRepository.loadContactsSnapshot(predicate = null) }
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `should load contact groups`() {
        coEvery { contactLoadRepository.loadAllContactGroups() } returns emptyList()

        runBlocking { underTest.getAllContactGroups() }

        coVerify { contactLoadRepository.loadAllContactGroups() }
    }

    @Test
    fun `should filter contact groups by account`() {
        val account = ContactAccount.OnlineAccount(username = "Tywin", accountProvider = "Lannister")
        val internetAccount = someInternetAccount(name = account.username, type = account.accountProvider)
        val expectedContactGroup = someAndroidContactGroup(title = "the right account", account = internetAccount)
        val contactGroups = listOf(
            expectedContactGroup,
            someAndroidContactGroup(title = "some other account", account = someInternetAccount("other")),
            someAndroidContactGroup(title = "no account", account = null),
        )
        coEvery { contactLoadRepository.loadAllContactGroups() } returns contactGroups

        val result = runBlocking { underTest.getContactGroups(account) }

        assertThat(result).hasSize(1)
        assertThat(result.first().id.name).isEqualTo(expectedContactGroup.title)
        assertThat(result.first().notes).isEqualTo(expectedContactGroup.note)
    }
}
